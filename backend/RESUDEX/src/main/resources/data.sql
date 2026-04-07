-- Default jobs so admin has something to work with from day 1
MERGE INTO jobs (id, title, description) KEY(id) VALUES
(1, 'Python Developer', 'Looking for a python django flask fastapi numpy pandas scikit-learn developer with 0-2 years experience in data and web projects.'),
(2, 'Java Backend Engineer', 'Need java spring spring boot hibernate jpa jdbc rest microservices maven developer with 2-4 years of strong backend experience.'),
(3, 'Full Stack Developer', 'Looking for java spring react javascript html css bootstrap tailwind node full stack developer with 1-3 years experience.'),
(4, 'DevOps Engineer', 'Need docker kubernetes aws azure ci/cd jenkins linux cloud expert with 3-6 years experience.'),
(5, 'Data Engineer', 'Python sql pandas spark data pipeline numpy statistics machine learning experience needed. Required: 2-5 years.'),
(6, 'C++ Software Engineer', 'Seeking a C++ Developer with experience in STL, Pointers, and Data Structures. Embedded systems knowledge is a plus. Range: 1-4 years.');

