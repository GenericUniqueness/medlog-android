# MedLog Decision Log

## Decision 1: Kotlin + Jetpack Compose
- **Choice**: Kotlin with Jetpack Compose for UI
- **Why**: Kotlin is the idiomatic Android language. Compose eliminates XML layout boilerplate and enables declarative UI with natural ViewModel integration.
- **Trade-off**: Compose is newer than XML views; some edge-case solutions are less documented.
- **Mitigation**: Use Material 3 components which have mature Compose support.

## Decision 2: Room over Raw SQLite
- **Choice**: Room ORM for database access
- **Why**: Compile-time query validation, type-safe DAOs, Flow-based reactive queries, and migration support.
- **Trade-off**: Slight overhead from annotation processing.
- **Mitigation**: Acceptable for 13 entities with moderate query complexity.

## Decision 3: Manual DI over Hilt
- **Choice**: Singleton AppContainer created in MedLogApp
- **Why**: Single-module offline app doesn't need Hilt's complexity. Manual DI is debuggable, has zero build-time cost, and is sufficient for our scope.
- **Trade-off**: No automatic scoping or dependency graph validation.
- **Mitigation**: Clear constructor dependencies make manual wiring straightforward.

## Decision 4: AlarmManager + WorkManager for Reminders
- **Choice**: AlarmManager for exact-timing reminders, WorkManager for boot rescheduling
- **Why**: WorkManager alone cannot guarantee exact-time delivery. AlarmManager with setExactAndAllowWhileIdle provides reliable medication reminders.
- **Trade-off**: More code than using WorkManager alone.
- **Mitigation**: BootReceiver enqueues WorkManager to re-schedule all alarms after reboot.

## Decision 5: Internal Storage for File Attachments
- **Choice**: Files stored in context.filesDir with profile-scoped subdirectories
- **Why**: No scoped storage complications, no permissions needed for internal storage, files are private to the app.
- **Trade-off**: Files not accessible from outside the app (can't be browsed in file manager).
- **Mitigation**: Export to PDF feature allows sharing data externally.

## Decision 6: Feature-Based Package Structure
- **Choice**: Group files by feature (medication, condition, etc.) rather than by layer (viewmodel, screen, etc.)
- **Why**: Easier to navigate and modify a single feature when all related files are together.
- **Trade-off**: Some duplication of patterns across features.
- **Mitigation**: Shared components package reduces duplication.

## Decision 7: Polymorphic File Attachments
- **Choice**: entityType + entityId columns in FileAttachment rather than separate join tables
- **Why**: Simpler schema, single table for all attachments, easy to query by entity type.
- **Trade-off**: No foreign key enforcement at the database level for the entity relationship.
- **Mitigation**: Application-layer validation ensures referential integrity.

## Decision 8: PDFBox for PDF Export
- **Choice**: tomdog/pdfbox-android library for PDF generation
- **Why**: Mature PDF library with Android port, supports structured document creation.
- **Trade-off**: Adds ~2MB to APK size.
- **Mitigation**: PDF export is an optional feature; the library is only used in settings.
