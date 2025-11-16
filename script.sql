DROP TABLE IF EXISTS accruals CASCADE;
DROP TABLE IF EXISTS grounds CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS payrolls CASCADE;
DROP TABLE IF EXISTS scholarship_types CASCADE;
DROP TABLE IF EXISTS student_groups CASCADE;
DROP TABLE IF EXISTS statuses CASCADE;

CREATE TABLE statuses (
    id SERIAL PRIMARY KEY,
    status_code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100)
);

CREATE TABLE student_groups (
    id SERIAL PRIMARY KEY,
    group_code VARCHAR(50) NOT NULL UNIQUE,
    course INT NOT NULL,
    faculty VARCHAR(100)
);

CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    fio VARCHAR(200) NOT NULL,
    group_id INT NOT NULL,
    avg_grade DECIMAL(3,2),
    has_social_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (group_id) REFERENCES student_groups(id)
);

CREATE TABLE scholarship_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    base_amount DECIMAL(10,2) NOT NULL,
    requires_docs BOOLEAN DEFAULT FALSE
);

CREATE TABLE grounds (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL,
    type_id INT NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    valid_until DATE,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (type_id) REFERENCES scholarship_types(id)
);

CREATE TABLE payrolls (
    id SERIAL PRIMARY KEY,
    for_month DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_id INT NOT NULL,
    file_path VARCHAR(500),
    FOREIGN KEY (status_id) REFERENCES statuses(id)
);

CREATE TABLE accruals (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL,
    type_id INT NOT NULL,
    payroll_id INT,
    amount DECIMAL(10,2) NOT NULL,
    for_month DATE NOT NULL,
    status_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (type_id) REFERENCES scholarship_types(id),
    FOREIGN KEY (payroll_id) REFERENCES payrolls(id),
    FOREIGN KEY (status_id) REFERENCES statuses(id),
    CONSTRAINT unique_accrual UNIQUE (student_id, for_month, type_id)
);

INSERT INTO statuses (status_code, description) VALUES
('draft', 'Черновик'),
('calculated', 'Рассчитано'),
('paid', 'Выплачено'),
('cancelled', 'Отменено'),
('Сформирована', 'Статус ведомости сформирована'),
('Черновик', 'Статус ведомости черновик');

INSERT INTO student_groups (group_code, course, faculty) VALUES
('ИСП-21', 2, 'Информационные технологии'),
('ИСП-31', 3, 'Информационные технологии'),
('ЭК-21', 2, 'Экономический факультет');

INSERT INTO students (fio, group_id, avg_grade, has_social_status) VALUES
('Иванов Иван Иванович', 1, 4.80, FALSE),
('Петрова Мария Сергеевна', 1, 4.95, TRUE),
('Сидоров Петр Алексеевич', 2, 4.50, FALSE),
('Козлова Анна Викторовна', 2, 4.70, TRUE),
('Смирнов Дмитрий Михайлович', 3, 4.20, FALSE),
('Новикова Елена Павловна', 3, 4.85, TRUE);

INSERT INTO scholarship_types (name, base_amount, requires_docs) VALUES
('Академическая', 2500.00, FALSE),
('Губернаторская', 5000.00, FALSE),
('Социальная', 3500.00, TRUE);

INSERT INTO grounds (student_id, type_id, doc_type, issue_date, valid_until) VALUES
(1, 2, 'приказ', '2024-09-01', '2025-01-31'),
(2, 2, 'приказ', '2024-09-01', '2025-01-31'),
(2, 3, 'справка', '2024-08-15', '2025-08-15'),
(3, 1, 'приказ', '2024-09-01', '2025-01-31'),
(4, 2, 'конкурс', '2024-09-01', '2025-01-31'),
(4, 3, 'справка', '2024-07-20', '2025-07-20'),
(6, 2, 'приказ', '2024-09-01', '2025-01-31'),
(6, 3, 'справка', '2024-08-10', '2025-08-10');

INSERT INTO payrolls (for_month, status_id) VALUES
('2024-09-01', 5),
('2024-10-01', 5),
('2024-11-01', 6);

INSERT INTO accruals (student_id, type_id, payroll_id, amount, for_month, status_id) VALUES
(1, 2, 1, 5000.00, '2024-09-01', 2),
(2, 2, 1, 5000.00, '2024-09-01', 2),
(2, 3, 1, 3500.00, '2024-09-01', 2),
(3, 1, 1, 2500.00, '2024-09-01', 2),
(4, 2, 1, 5000.00, '2024-09-01', 2),
(4, 3, 1, 3500.00, '2024-09-01', 2),
(6, 2, 1, 5000.00, '2024-09-01', 2),
(6, 3, 1, 3500.00, '2024-09-01', 2);

CREATE INDEX idx_students_group_id ON students(group_id);
CREATE INDEX idx_students_avg_grade ON students(avg_grade);
CREATE INDEX idx_students_social_status ON students(has_social_status);

CREATE INDEX idx_grounds_student_id ON grounds(student_id);
CREATE INDEX idx_grounds_type_id ON grounds(type_id);
CREATE INDEX idx_grounds_dates ON grounds(issue_date, valid_until);

CREATE INDEX idx_accruals_student_id ON accruals(student_id);
CREATE INDEX idx_accruals_type_id ON accruals(type_id);
CREATE INDEX idx_accruals_payroll_id ON accruals(payroll_id);
CREATE INDEX idx_accruals_for_month ON accruals(for_month);
CREATE INDEX idx_accruals_status_id ON accruals(status_id);

CREATE INDEX idx_payrolls_for_month ON payrolls(for_month);
CREATE INDEX idx_payrolls_status_id ON payrolls(status_id);
CREATE INDEX idx_payrolls_created_at ON payrolls(created_at);
