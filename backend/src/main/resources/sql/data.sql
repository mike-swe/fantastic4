INSERT INTO users (id, username, password, email, role) VALUES
(randomblob(16),'admin', 'password123', 'admin@fantastic4.com', 'ADMIN'),
(randomblob(16),'tester1', 'password123', 'tester1@fantastic4.com', 'TESTER'),
(randomblob(16),'dev1', 'password123', 'dev1@fantastic4.com', 'DEVELOPER');