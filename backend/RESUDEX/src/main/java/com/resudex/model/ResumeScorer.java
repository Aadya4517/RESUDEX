package com.resudex.model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// core scoring engine
public class ResumeScorer {

    private static final Map<String, Set<String>> DOMAIN_SKILLS = Map.of(
            "Java Backend", Set.of(
                    "java","spring","spring boot","hibernate","jpa",
                    "jdbc","maven","gradle","rest","microservices","tomcat","jetty","servlets"
            ),
            "Web Development", Set.of(
                    "html","css","javascript","react","angular",
                    "vue","bootstrap","tailwind","node","typescript","express","sass","less","jquery","webpack"
            ),
            "Python", Set.of(
                    "python","django","flask","fastapi",
                    "numpy","pandas","scikit-learn","tensorflow","pytorch","keras","matplotlib"
            ),
            "C / C++", Set.of(
                    "c","c++","stl","pointers",
                    "data structures","memory management","cmake","make","embedded","firmware"
            ),
            "DevOps", Set.of(
                    "docker","kubernetes","aws","azure",
                    "gcp","ci/cd","jenkins","linux","terraform","ansible","prometheus","grafana"
            ),
            "Databases", Set.of(
                    "mysql","postgresql","mongodb","redis","sql","oracle","sqlite","cassandra","elasticsearch"
            ),
            "Cloud & Misc", Set.of(
                    "api","git","agile","jira","scrum","unit testing","junit","mockito","rest api","json","xml"
            ),
            "General Tech & Roles", Set.of(
                    "software","engineer","developer","programmer","coder","manager","lead","senior","junior","intern",
                    "architect","analyst","specialist","consultant","fullstack","frontend","backend","office","cloud"
            ),
            "Impact & Leadership", Set.of(
                    "managed","led","mentored","orchestrated","architected","delivered","spearheaded","coordinated",
                    "impact","leadership","strategic","optimization","scale","revenue","performance"
            )
    );

    private static final Map<String, Set<String>> ROLE_SKILLS = Map.of(
            "Python Developer", Set.of("python","django","flask","fastapi","sql"),
            "Java Backend Engineer", Set.of("java","spring","sql","rest","microservices","hibernate"),
            "Full Stack Developer", Set.of("java","spring","react","javascript","sql","html","css"),
            "Data Engineer", Set.of("python","sql","pandas","spark","numpy"),
            "DevOps Engineer", Set.of("docker","kubernetes","aws","ci/cd","jenkins","linux")
    );

    // scan resume against job
    public ResumeScore scan(String fn, String txt, String jd) {
        SectionedResume sr = new SectionedResume(txt);
        jd = jd.toLowerCase();

        int exp = calcExp(txt);

        Set<String> hits = new HashSet<>();
        Set<String> miss = new HashSet<>();
        Set<String> must = getMust(jd);
        Set<String> missMust = new HashSet<>();

        Map<String, Integer> domains = new LinkedHashMap<>();
        Map<String, Integer> roles = new LinkedHashMap<>();
        List<String> recs = new ArrayList<>();

        // sectional weighted scan
        double wSc = 0;

        // implied skills
        Set<String> implied = new HashSet<>();
        String ltxt = txt.toLowerCase();
        if (ltxt.contains("c++") || ltxt.contains("c programming") || ltxt.contains("c language")) {
            implied.add("pointers"); implied.add("stl"); implied.add("memory management");
        }
        if (ltxt.contains("java")) { implied.add("oop"); implied.add("object oriented"); }
        if (ltxt.contains("python")) { implied.add("scripting"); }
        if (ltxt.contains("react")) { implied.add("javascript"); implied.add("html"); implied.add("css"); }
        if (ltxt.contains("spring boot") || ltxt.contains("spring")) { implied.add("java"); implied.add("rest"); }

        for (String s : must) {
            String ls = s.toLowerCase();
            boolean ok = false;
            double w = 0;

            if (implied.contains(ls)) {
                w = 0.9; ok = true;
            } else if (sr.hasSkill("EXPERIENCE", ls)) {
                w = 2.0; ok = true;
            } else if (sr.hasSkill("PROJECTS", ls)) {
                w = 1.5; ok = true;
            } else if (sr.hasSkill("SKILLS", ls)) {
                w = 1.0; ok = true;
            } else if (txt.toLowerCase().contains(ls)) {
                w = 0.8; ok = true;
            }

            if (ok) {
                hits.add(s);
                wSc += w;
            } else {
                missMust.add(s);
                miss.add(s);
            }
        }

        // domain fit
        for (Map.Entry<String, Set<String>> e : DOMAIN_SKILLS.entrySet()) {
            String dom = e.getKey();
            Set<String> sks = e.getValue();
            long cnt = sks.stream().filter(txt.toLowerCase()::contains).count();
            domains.put(dom, (int)((cnt * 100.0) / sks.size()));
        }

        // role fit
        for (Map.Entry<String, Set<String>> e : ROLE_SKILLS.entrySet()) {
            String role = e.getKey();
            Set<String> sks = e.getValue();
            long cnt = sks.stream().filter(txt.toLowerCase()::contains).count();
            roles.put(role, (int)((cnt * 100.0) / sks.size()));
        }

        // scoring
        double ratio = must.isEmpty() ? 0 : wSc / (must.size() * 2.0);
        int scSk = (int) (ratio * 70);

        int scExp = 0;
        int[] range = calcRange(jd);
        if (range != null) {
            int min = range[0], max = range[1];
            if (exp >= min && exp <= max) scExp = 25;
            else if (exp >= min - 1 && exp <= max + 1) scExp = 18;
            else if (exp >= min) scExp = 12;
            else scExp = 5;
        } else {
            if (exp >= 5) scExp = 25;
            else if (exp >= 3) scExp = 18;
            else if (exp >= 1) scExp = 10;
        }

        int boostRole = 0;
        String lTxt = txt.toLowerCase();
        String lTit = jd.split("\n")[0].toLowerCase().replace(" engineer", "").replace(" developer", "").trim();

        if (!lTit.isEmpty() && lTxt.contains(lTit)) {
            boostRole = 35;
        } else {
            for (String rNm : ROLE_SKILLS.keySet()) {
                String clean = rNm.toLowerCase().replace(" engineer", "").replace(" developer", "");
                if (lTxt.contains(clean) && jd.toLowerCase().contains(rNm.toLowerCase())) {
                    boostRole = 25;
                    break;
                }
            }
        }

        int boostRec = 0;
        if (!hits.isEmpty()) {
            long rHits = hits.stream().filter(lTxt::contains).count();
            boostRec = (int) Math.min(10, (rHits * 10.0) / hits.size());
        }

        int coreSc = 0;
        Set<String> terms = DOMAIN_SKILLS.get("General Tech & Roles");
        long cnt = terms.stream().filter(lTxt::contains).count();
        if (cnt > 0) coreSc = (int) Math.min(15, 5 + (cnt * 2));

        String jobDom = "General Tech & Roles";
        int bestM = 0;
        for (String d : DOMAIN_SKILLS.keySet()) {
            if (d.equals("General Tech & Roles") || d.equals("Impact & Leadership")) continue;
            long m = DOMAIN_SKILLS.get(d).stream().filter(jd.toLowerCase()::contains).count();
            if (m > bestM) { bestM = (int) m; jobDom = d; }
        }

        int domFit = domains.getOrDefault(jobDom, 0);
        int scDom = (domFit > 0) ? (5 + (domFit / 4)) : 0;

        int boostLead = 0;
        Set<String> iTerms = DOMAIN_SKILLS.get("Impact & Leadership");
        long iCnt = iTerms.stream().filter(lTxt::contains).count();
        if (iCnt > 0) boostLead = (int) Math.min(15, iCnt * 3);

        int penalty = Math.min(10, missMust.size() * 1);

        int sc = scSk + scExp + boostRole + boostRec + scDom + coreSc + boostLead - penalty;

        if (domFit < 10 && !jobDom.equals("General Tech & Roles")) sc = Math.min(sc, 45);
        if (!hits.isEmpty() && sc < 15) sc = 15;
        sc = Math.max(0, Math.min(sc, 100));

        // recommendations
        recs.add("Analyzed " + must.size() + " must-have skills");
        recs.add("Found " + hits.size() + " matches in key resume sections");
        recs.add("Experience: " + exp + " years total");
        recs.add("Missing mandatory: " + missMust.size());

        SkillGapPriority p = (missMust.size() >= 4) ? SkillGapPriority.HIGH : (missMust.size() >= 2 ? SkillGapPriority.MEDIUM : SkillGapPriority.LOW);

        List<String> roadmap = new ArrayList<>();
        if (missMust.isEmpty()) {
            roadmap.add("🎯 You match all required skills for this role!");
            roadmap.add("💡 Focus on polishing your portfolio and preparing for system design interviews.");
            roadmap.add("📝 Tailor your resume summary to highlight your strongest matching skills.");
        } else {
            roadmap.add("🗺️ Your personalized learning path to land this role:");
            int step = 1;
            for (String skill : missMust.stream().limit(6).collect(Collectors.toList())) {
                String s = skill.toLowerCase();
                String advice;
                // Java ecosystem
                if (s.equals("spring") || s.equals("spring boot"))
                    advice = "Step " + step + ": Learn Spring Boot — start with spring.io/guides, build a REST API with CRUD operations.";
                else if (s.equals("hibernate") || s.equals("jpa"))
                    advice = "Step " + step + ": Master JPA/Hibernate — practice entity mapping, relationships, and JPQL queries.";
                else if (s.equals("microservices"))
                    advice = "Step " + step + ": Study Microservices — learn service discovery (Eureka), API gateway, and inter-service communication.";
                else if (s.equals("maven") || s.equals("gradle"))
                    advice = "Step " + step + ": Learn " + skill + " — understand dependency management, build lifecycle, and multi-module projects.";
                else if (s.equals("jdbc"))
                    advice = "Step " + step + ": Practice JDBC — connect to a database, write prepared statements, and handle transactions.";
                // Python ecosystem
                else if (s.equals("django"))
                    advice = "Step " + step + ": Learn Django — build a web app with models, views, templates, and Django REST Framework.";
                else if (s.equals("flask") || s.equals("fastapi"))
                    advice = "Step " + step + ": Build a REST API with " + skill + " — cover routing, request handling, and authentication.";
                else if (s.equals("pandas"))
                    advice = "Step " + step + ": Master pandas — practice DataFrame operations, groupby, merge, and data cleaning on a real dataset.";
                else if (s.equals("numpy"))
                    advice = "Step " + step + ": Learn NumPy — focus on array operations, broadcasting, and matrix math for data pipelines.";
                else if (s.equals("scikit-learn"))
                    advice = "Step " + step + ": Explore scikit-learn — implement classification, regression, and cross-validation on a Kaggle dataset.";
                else if (s.equals("tensorflow") || s.equals("pytorch"))
                    advice = "Step " + step + ": Start with " + skill + " — build a simple neural network, then explore CNNs or RNNs.";
                // C/C++
                else if (s.equals("stl"))
                    advice = "Step " + step + ": Master C++ STL — practice vector, map, set, and algorithm headers with competitive programming problems.";
                else if (s.equals("pointers"))
                    advice = "Step " + step + ": Strengthen pointer skills — practice pointer arithmetic, dynamic memory, and linked list implementations.";
                else if (s.equals("embedded") || s.equals("firmware"))
                    advice = "Step " + step + ": Learn embedded systems — study GPIO, interrupts, and UART on an Arduino or STM32 board.";
                else if (s.equals("memory management"))
                    advice = "Step " + step + ": Study memory management — understand heap vs stack, RAII, and smart pointers in C++.";
                // Web
                else if (s.equals("react"))
                    advice = "Step " + step + ": Learn React — build a project using hooks (useState, useEffect), React Router, and a REST API.";
                else if (s.equals("node") || s.equals("node.js"))
                    advice = "Step " + step + ": Learn Node.js — build an Express server with REST endpoints, middleware, and JWT auth.";
                else if (s.equals("typescript"))
                    advice = "Step " + step + ": Add TypeScript to your stack — practice interfaces, generics, and strict typing in a React or Node project.";
                else if (s.equals("tailwind") || s.equals("bootstrap"))
                    advice = "Step " + step + ": Learn " + skill + " — rebuild a UI component from scratch using the framework's utility classes.";
                // DevOps
                else if (s.equals("docker"))
                    advice = "Step " + step + ": Learn Docker — containerize an existing app, write a Dockerfile, and use docker-compose for multi-service setup.";
                else if (s.equals("kubernetes"))
                    advice = "Step " + step + ": Study Kubernetes — deploy a containerized app on Minikube, learn pods, services, and deployments.";
                else if (s.equals("aws"))
                    advice = "Step " + step + ": Get AWS hands-on — use EC2, S3, and Lambda via the free tier. Aim for AWS Cloud Practitioner cert.";
                else if (s.equals("azure"))
                    advice = "Step " + step + ": Explore Azure — deploy an app to Azure App Service and study Azure DevOps pipelines.";
                else if (s.equals("ci/cd"))
                    advice = "Step " + step + ": Set up a CI/CD pipeline — use GitHub Actions or Jenkins to automate build, test, and deploy.";
                else if (s.equals("linux"))
                    advice = "Step " + step + ": Strengthen Linux skills — practice shell scripting, file permissions, process management, and cron jobs.";
                else if (s.equals("terraform") || s.equals("ansible"))
                    advice = "Step " + step + ": Learn " + skill + " — write infrastructure-as-code to provision and configure cloud resources.";
                // Databases
                else if (s.equals("sql") || s.equals("mysql") || s.equals("postgresql"))
                    advice = "Step " + step + ": Deepen SQL skills — practice JOINs, subqueries, window functions, and query optimization.";
                else if (s.equals("mongodb"))
                    advice = "Step " + step + ": Learn MongoDB — practice CRUD, aggregation pipelines, and indexing strategies.";
                else if (s.equals("redis"))
                    advice = "Step " + step + ": Explore Redis — implement caching, pub/sub, and session management in a small project.";
                // General
                else if (s.equals("rest") || s.equals("rest api"))
                    advice = "Step " + step + ": Master REST API design — study HTTP methods, status codes, versioning, and OpenAPI/Swagger docs.";
                else if (s.equals("git"))
                    advice = "Step " + step + ": Level up Git — practice branching strategies (GitFlow), rebasing, and resolving merge conflicts.";
                else if (s.equals("java"))
                    advice = "Step " + step + ": Strengthen Java — focus on OOP principles, Collections, Streams, and concurrency basics.";
                else if (s.equals("python"))
                    advice = "Step " + step + ": Deepen Python — study decorators, generators, context managers, and async programming.";
                else if (s.equals("javascript"))
                    advice = "Step " + step + ": Master JavaScript — study closures, promises, async/await, and the event loop.";
                else
                    advice = "Step " + step + ": Learn " + skill + " — find the official docs, build a small project using it, then add it to your portfolio.";

                roadmap.add(advice);
                step++;
            }
            if (missMust.size() > 6) {
                roadmap.add("📌 " + (missMust.size() - 6) + " more skills to cover: " +
                    missMust.stream().skip(6).collect(Collectors.joining(", ")));
            }
            roadmap.add("⏱️ Estimated time to close this gap: " +
                (p == SkillGapPriority.HIGH ? "3–5 months" : p == SkillGapPriority.MEDIUM ? "4–8 weeks" : "1–2 weeks"));
        }

        return new ResumeScore(
                fn, sc, exp, hits, miss, missMust,
                "Human-readable deep scan completed.",
                100, Set.of(), p, roadmap, roles, domains, recs
        );
    }

    // calc experience years
    private int calcExp(String txt) {
        Pattern p = Pattern.compile("(\\d+)\\s*(years?|yrs?|months?)");
        Matcher m = p.matcher(txt);
        int mo = 0;
        while (m.find()) {
            int v = Integer.parseInt(m.group(1));
            String u = m.group(2);
            if (u.contains("year") || u.contains("yr")) mo += v * 12;
            else mo += v;
        }
        return mo / 12;
    }

    // calc exp range from jd
    private int[] calcRange(String jd) {
        Pattern p = Pattern.compile("(\\d+)-(\\d+)\\s*(years?|yrs?)");
        Matcher m = p.matcher(jd.toLowerCase());
        if (m.find()) return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        return null;
    }

    // get must-have skills
    private Set<String> getMust(String jd) {
        Set<String> must = new HashSet<>();
        String low = jd.toLowerCase();
        for (Set<String> sks : DOMAIN_SKILLS.values()) {
            for (String s : sks) {
                Pattern p = Pattern.compile("\\b" + Pattern.quote(s) + "\\b", Pattern.CASE_INSENSITIVE);
                if (p.matcher(low).find()) must.add(s);
            }
        }
        if (must.isEmpty() && (low.contains("job") || low.contains("role"))) must.add("software");
        return must;
    }
}
