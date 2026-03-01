const form = document.getElementById("uploadForm");
const fileInput = document.getElementById("resumes");
const fileList = document.getElementById("fileList");
const resultsDiv = document.getElementById("results");
const summaryDiv = document.getElementById("summary");
const loadingDiv = document.getElementById("loading");
const emptyState = document.getElementById("emptyState");
const analyzeBtn = document.querySelector("button[type='submit']");
const dropZone = document.getElementById("dropZone");
const exportBtn = document.getElementById("exportPdfBtn");

let files = [];
window.lastResultData = null;

/* FILE INPUT */
fileInput.addEventListener("change", e => {
    for (let f of e.target.files) {
        if (!files.find(existing => existing.name === f.name)) {
            files.push(f);
        }
    }
    renderFileList();
});

/* DRAG & DROP */
dropZone.addEventListener("dragover", e => {
    e.preventDefault();
});

dropZone.addEventListener("drop", e => {
    e.preventDefault();
    for (let f of e.dataTransfer.files) {
        if (!files.find(existing => existing.name === f.name)) {
            files.push(f);
        }
    }
    renderFileList();
});

function renderFileList() {
    fileList.innerHTML = files
        .map(f => `<div class="file-pill">📄 ${f.name}</div>`)
        .join("");
}

function scoreColor(score) {
    if (score >= 80) return "#16a34a";
    if (score >= 50) return "#f59e0b";
    return "#dc2626";
}

/* SUBMIT */
form.addEventListener("submit", async e => {
    e.preventDefault();

    if (files.length === 0) {
        alert("Please upload at least one resume.");
        return;
    }

    emptyState.classList.add("hidden");
    loadingDiv.classList.remove("hidden");
    analyzeBtn.disabled = true;
    resultsDiv.innerHTML = "";

    const fd = new FormData();
    files.forEach(f => fd.append("files", f));
    fd.append("jobDescription", jobDescription.value);

    const res = await fetch("http://localhost:8080/api/uploadMultiple", {
        method: "POST",
        body: fd
    });

    const data = await res.json();
    window.lastResultData = data;

    summaryDiv.classList.remove("hidden");
    summaryDiv.innerHTML =
        `Total: ${data.totalResumes} | Highest: ${data.highestScore}% | Average: ${Math.round(data.averageScore)}%`;

    renderResults(data.rankedResumes);

    loadingDiv.classList.add("hidden");
    analyzeBtn.disabled = false;

    exportBtn.classList.remove("hidden");
});

/* RESULTS */
function renderResults(resumes) {
    resultsDiv.innerHTML = "";

    resumes.forEach((r, i) => {
        const card = document.createElement("div");
        card.className = "resume-card";

      card.innerHTML = `
    <div class="card-header">
        <span class="rank">#${i + 1}</span>
        <strong>${r.fileName}</strong>
        <span class="score" style="background:${scoreColor(r.finalScore)}">
            ${r.finalScore}%
        </span>
    </div>

    <div class="skill-gap-badge ${r.skillGapPriority}">
        Skill Gap: ${r.missingSkills.length} (${r.skillGapPriority})
    </div>

    <div class="toggle"
         onclick="this.nextElementSibling.classList.toggle('expand')">
         ▶ Why this rank?
    </div>

    <div class="why-section">
        ${r.rankSummary.map(x => `<div>• ${x}</div>`).join("")}

        <div class="skill-gap-details">
            <strong>Missing Skills:</strong><br>
            ${r.missingSkills.join(", ")}
        </div>
    </div>

    <h4>Domain Fit</h4>
    ${renderBars(r.domainFit)}

    <h4>Role Fit</h4>
    ${renderBars(r.roleFit)}
`;  

        resultsDiv.appendChild(card);
    });
}

function renderBars(data) {
    let html = "";
    for (let key in data) {
        html += `
            <div class="bar-row">
                <span>${key}</span>
                <div class="bar-bg">
                    <div class="bar-fill" style="width:${data[key]}%"></div>
                </div>
                <span>${data[key]}%</span>
            </div>
        `;
    }
    return html;
}

/* BUILD PDF REPORT */
function buildPdfReport(data) {

    const win = window.open('', '', 'width=900,height=700');

    const html = `
    <html>
    <head>
        <title>Resudex Report</title>
        <style>
            body { font-family: Arial; padding: 20px; }
            h1 { text-align:center; }
            table { width:100%; border-collapse: collapse; margin-top:20px; }
            th, td {
                border:1px solid #ccc;
                padding:8px;
                text-align:left;
                font-size:12px;
            }
            th { background:#f3f4f6; }
        </style>
    </head>
    <body>
        <h1>Resudex Resume Analysis Report</h1>

        <p><strong>Total:</strong> ${data.totalResumes}</p>
        <p><strong>Highest:</strong> ${data.highestScore}%</p>
        <p><strong>Average:</strong> ${Math.round(data.averageScore)}%</p>

        <table>
            <tr>
                <th>Rank</th>
                <th>File</th>
                <th>Score</th>
                <th>Matched Skills</th>
                <th>Missing Skills</th>
            </tr>

            ${data.rankedResumes.map((r,i)=>`
                <tr>
                    <td>${i+1}</td>
                    <td>${r.fileName}</td>
                    <td>${r.finalScore}%</td>
                    <td>${r.matchedSkills.join(", ")}</td>
                    <td>${r.missingSkills.join(", ")}</td>
                </tr>
            `).join("")}
        </table>
    </body>
    </html>
    `;

    win.document.write(html);
    win.document.close();

    setTimeout(() => {
        win.print();
    }, 500);
}

/* PDF EXPORT */
exportBtn.addEventListener("click", () => {
    buildPdfReport(window.lastResultData, jobDescription.value);

    const element = document.getElementById("pdfReport");

    html2pdf().from(element).set({
        margin: 10,
        filename: "Resudex_Report.pdf",
        html2canvas: { scale: 2 },
        jsPDF: { unit: "mm", format: "a4", orientation: "portrait" }
    }).save();
});