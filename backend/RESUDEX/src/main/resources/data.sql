-- Default jobs so admin has something to work with from day 1
MERGE INTO jobs (id, title, description) KEY(id) VALUES
(1, 'Python Developer', 'Looking for a python django flask fastapi numpy pandas scikit-learn developer with 2 years experience in data and web projects.'),
(2, 'Java Backend Engineer', 'Need java spring spring boot hibernate jpa jdbc rest microservices maven developer with strong backend skills.'),
(3, 'Full Stack Developer', 'Looking for java spring react javascript html css bootstrap tailwind node full stack developer.'),
(4, 'DevOps Engineer', 'Need docker kubernetes aws azure ci/cd jenkins linux cloud expert with at least 3 years experience.'),
(5, 'Data Engineer', 'Python sql pandas spark data pipeline numpy statistics machine learning experience needed.');

