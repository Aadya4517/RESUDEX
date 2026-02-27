document.getElementById("uploadForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const files = document.getElementById("resumes").files;
    const jobDescription = document.getElementById("jobDescription").value;
    const loading = document.getElementById("loading");

    if (files.length === 0) {
        alert("Upload at least one resume.");
        return;
    }

    const formData = new FormData();

    for (let file of files) {
        formData.append("files", file);
    }

    formData.append("jobDescription", jobDescription);

    loading.classList.remove("hidden");

    try {
        const response = await fetch("http://localhost:8080/api/uploadMultiple", {
            method: "POST",
            body: formData
        });

        const data = await response.json();
        renderCards(data);
    } catch (error) {
        alert("Backend connection failed.");
    }

    loading.classList.add("hidden");
});

function renderCards(data) {
    const container = document.getElementById("results");
    container.innerHTML = "";

    data.forEach((item, index) => {
        const card = document.createElement("div");
        card.className = "resume-card";

        if (index === 0) {
            card.classList.add("top");
        }

        card.innerHTML = `
            <h3>${item.fileName}</h3>
            <p><strong>Rank:</strong> ${index + 1}</p>
            <p><strong>Experience:</strong> ${item.experienceYears} years</p>
            <div class="score-bar">
                <div class="score-fill" style="width:${item.finalScore}%"></div>
            </div>
            <p><strong>Score:</strong> ${item.finalScore}%</p>
            <p><strong>Matched Skills:</strong> ${item.matchedSkills.join(", ")}</p>
        `;

        container.appendChild(card);
    });
}