# Database

## MySQL schema

`database/schema.sql` creates the `employee_management` database and four tables:

| Table | Purpose | Important constraints |
|---|---|---|
| `users` | Usernames, BCrypt password hashes, and enabled state. | Unique username; username length 80; password column length 100. |
| `roles` | Role definitions. | Unique role name, length 50. |
| `user_roles` | User-to-role assignments. | Composite primary key and foreign keys to `users` and `roles`. |
| `employees` | Employee information. | Unique email; department and last-name indexes. |

Employee salary is `DECIMAL(12,2)`, dates use `DATE`, and active/enabled flags use `BOOLEAN`. JPA mappings are kept aligned with the SQL column lengths and employee indexes.

## Schema lifecycle

The production default is `spring.jpa.hibernate.ddl-auto=validate`. Create or migrate tables explicitly before deploying a schema change; do not switch production to `create`, `create-drop`, or `update` as a substitute for reviewed migrations. The current repository includes the initial SQL schema and does not include a migration framework.

The application initializes the `USER` and `ADMIN` role rows if they are missing. It does not create a default user or administrator. Bootstrap administrator access through an authorized operational process.

## Configuration

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the application environment. Defaults target a local MySQL instance and the `employee_management` database; no real password is included in the repository. Keep database credentials out of logs, source control, and support messages.

The `test` profile uses an in-memory H2 database with MySQL compatibility mode and `create-drop`. Automated tests do not connect to a developer's MySQL database or mutate its records.

## Docker Compose

Compose mounts `database/schema.sql` into the MySQL image's initialization directory. MySQL runs these scripts only when it initializes a new data directory. Compose uses a named persistent volume; removing or replacing it destroys that container database, so do not do so if its data must be retained. SQL edits do not automatically migrate existing volumes.
