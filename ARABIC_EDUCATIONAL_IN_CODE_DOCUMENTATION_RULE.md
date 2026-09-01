# ARABIC_EDUCATIONAL_IN_CODE_DOCUMENTATION_RULE

## Purpose

This rule establishes a permanent and strict standard for adding clear, accurate, educational Arabic documentation inside the application's source files.

The purpose is to make the source code understandable to an Arabic-speaking reader who may not know the application's programming language or may have limited programming knowledge.

The goal is not to translate programming syntax word by word.

The goal is to explain the meaning, logic, purpose, flow, and relationships of the code progressively, so the reader can understand the file while moving through it.

---

# ABSOLUTE CODE PRESERVATION RULE

The existing application code is the functional implementation of the application and must not be modified during this documentation process.

Do not change:

- Application logic.
- Code behavior.
- Functionality.
- Algorithms.
- Conditions.
- Expressions.
- Variables.
- Function names.
- Class names.
- Parameters.
- Return types.
- Imports.
- Dependencies.
- Architecture.
- Data flow.
- Execution order.
- Existing behavior.

Do not:

- Refactor code.
- Optimize code.
- Fix code.
- Rewrite code.
- Reformat code unnecessarily.
- Move code.
- Delete code.
- Add executable code.
- Change any programming syntax.

The documentation task must be documentation only.

Only comments and documentation may be added or updated.

The purpose is to explain the existing code, not to alter it.

---

# COMPILATION SAFETY RULE

Because the existing source code is the actual application implementation, no action during the documentation process may introduce a compilation error.

Documentation changes must never:

- Break code syntax.
- Modify executable statements.
- Remove required syntax.
- Change imports.
- Change brackets or parentheses.
- Change string literals.
- Change code identifiers.
- Change annotations.
- Change function signatures.
- Change class definitions.

Before considering any file completed, verify that the documentation work has not altered the actual code and has not introduced any compilation problem.

The source code must remain functionally identical before and after the documentation process.

---

# Core Documentation Principle

Do not explain the entire file in one large documentation block.

Do not place all explanations at the beginning of the file.

Do not create one general summary followed by large unexplained blocks of code.

Do not mechanically translate every line of code.

Instead:

Divide the file into meaningful logical sections.

Before each important logical section, place an Arabic explanation that prepares the reader to understand the code that follows.

The reader should understand what the next code section does before reading that section.

The reading flow should be:

Arabic Explanation
↓
Code Section
↓
Arabic Explanation
↓
Next Code Section
↓
Arabic Explanation
↓
Next Code Section

Continue this pattern throughout the file.

---

# Required Explanation Before Each Logical Section

Before each meaningful code section, explain in clear Arabic:

- What this section is.
- What it is going to do.
- Why this section exists.
- What data or values it receives or uses.
- Where those data or values come from.
- What happened before this section.
- How this section connects to the previous section.
- What processing happens here.
- What result this section produces.
- Where the result goes.
- What will happen after this section.
- Which other components or files it connects to, when relevant.

The explanation must appear immediately before the code section it explains.

---

# Documentation Structure

The file must be documented according to its real logical structure.

Depending on the file, document important sections such as:

- The overall purpose of the file.
- Important imports.
- Class declarations.
- Object declarations.
- State definitions.
- Variables and properties.
- Initialization logic.
- Constructors.
- Functions and methods.
- Conditions and decision branches.
- Loops.
- Data transformations.
- Database operations.
- Network operations.
- Coroutine and asynchronous operations.
- Error handling.
- Results.
- Return values.
- Communication with other application components.

Do not document sections mechanically.

Explain each section according to its real purpose and importance.

---

# Arabic Writing Standard

All newly added or updated explanatory documentation must be written in Arabic that is:

- Clear.
- Correct.
- Professional.
- Elegant.
- Easy to understand.
- Educational.
- Technically accurate.
- Appropriate for a reader who does not deeply understand the programming language.

Use simple Arabic when possible.

Use technical terms when necessary, but explain them through context so that the reader can understand the purpose of the code.

The documentation should never be vague.

---

# Required Explanation Style

The documentation must explain the code naturally and progressively.

The preferred explanation should answer:

What happened before this section?

What is this section doing now?

Why is it necessary?

What data does it use?

What result will it produce?

What happens next?

How is it connected to other parts of the file or application?

The explanation should make the logical sequence clear:

Before
→
Now
→
Processing
→
Result
→
Next Step

Example of the intended meaning:

"This section receives the data prepared in the previous step."

"It first verifies that the required values are available before continuing."

"After successful verification, the data is passed to the component responsible for the next operation."

"The result produced here is then used by the following section to update the application state."

The documentation must help the reader follow the application's logic as a continuous story.

---

# Do Not Translate Code Mechanically

Do not create comments that simply repeat programming syntax.

Do not explain code in a meaningless way such as:

"This is an if statement."

"This variable stores a value."

"This function returns something."

Instead, explain:

- Why the condition is being checked.
- Why the value is being stored.
- What the function is responsible for.
- What problem the code solves.
- What happens when the condition succeeds or fails.
- How the result affects the next operation.

The purpose is understanding the meaning and logic, not translating keywords.

---

# Existing English Documentation

When processing a file:

- Review existing English comments and documentation.
- Convert meaningful English documentation into accurate Arabic.
- Preserve the original technical meaning.
- Expand the explanation when necessary to make the code easier to understand.
- Do not leave conflicting English documentation.
- Do not change the application code while translating documentation.

If an English comment is outdated or incorrect compared with the actual code, do not blindly preserve it.

The Arabic documentation must accurately describe the actual behavior of the existing code.

---

# Level of Detail

The documentation must be detailed enough to allow the reader to understand the file accurately.

However:

- Do not add useless comments to trivial syntax.
- Do not explain every character.
- Do not explain every keyword.
- Do not create repetitive comments.
- Do not overload simple code with unnecessary explanations.

The level of detail must follow the complexity of the code.

Simple logic:
Use a concise explanation.

Complex logic:
Provide a deeper explanation.

Important data flow:
Explain the source, processing, result, and destination.

The goal is complete understanding, not maximum comment quantity.

---

# Reading Experience Requirement

After documentation is added, the file should be understandable in this sequence:

1. The reader sees an Arabic explanation.
2. The reader understands what the next code section will do.
3. The reader reads the existing code.
4. The next Arabic explanation explains the following logical step.
5. The reader continues progressively through the file.
6. Important relationships and data flow become clear without requiring deep knowledge of the programming language.

The reader should not encounter a large and important block of unexplained logic when that logic can reasonably be explained in smaller logical sections.

---

# File Completion Requirements

A file is considered fully documented only when:

- The main purpose of the file is clear.
- Its important logical sections are explained in Arabic.
- The relationships between important sections are understandable.
- The flow of important data is understandable.
- Important dependencies are explained when relevant.
- Existing meaningful English documentation has been converted or incorporated into Arabic.
- The explanations match the actual existing code.
- The source code itself has not been changed.
- No compilation error has been introduced.
- The file can be followed progressively by a reader who does not deeply understand the programming language.

---

# Progress Tracking in Detailed_Roadmap.md

After a file has been fully processed:

1. Update Detailed_Roadmap.md.
2. Mark the file as completed.
3. Record that Arabic educational in-code documentation has been completed.
4. Continue to the next incomplete file according to the established sequence.

Before starting work:

1. Open Detailed_Roadmap.md.
2. Check the current progress.
3. Identify completed files.
4. Identify the first incomplete file.
5. Continue from that exact point.

If no file has been completed:

Start from the first file in the established sequence.

Do not repeat completed files unless the file has changed or the documentation requires a new revision.

---

# Mandatory Final Verification

Before marking a file as completed:

- Confirm that only documentation or comments were changed.
- Confirm that no executable code was modified.
- Confirm that no logic was changed.
- Confirm that no syntax was changed.
- Confirm that no imports or dependencies were changed.
- Confirm that the documentation matches the actual code.
- Confirm that no compilation error was introduced.
- Confirm that the file is correctly marked as completed in Detailed_Roadmap.md.

---

# Final Standard

The application source code must progressively become understandable to an Arabic-speaking reader through Arabic educational documentation embedded directly inside the existing source files.

The code must remain untouched.

Only the explanation around the code may be added or improved.

The reader should be able to understand, for every important part:

- What it does.
- Why it exists.
- What happened before it.
- What data it receives or uses.
- How it processes that data.
- What result it produces.
- Where the result goes.
- What happens next.
- How it relates to the rest of the file and application.

The final reading experience should feel like following the application's logic step by step in clear, professional, precise, and understandable Arabic, while the actual application code remains completely unchanged and compilation-safe.