# Templater for Rider - API Reference

Complete API documentation for all modules and commands.

---

## Table of Contents

1. [Date Module](#date-module)
2. [File Module](#file-module)
3. [Frontmatter Module](#frontmatter-module)
4. [System Module](#system-module)
5. [Web Module](#web-module)
6. [Obsidian Module](#obsidian-module)
7. [Config Module](#config-module)
8. [Script Engine](#script-engine)

---

## Date Module

**Namespace**: `tp.date`

Provides date and time manipulation functions.

### `tp.date.now(format?)`

Returns the current date and time.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted date/time

**Examples:**
```javascript
<% tp.date.now() %>
// Output: 2025-12-14

<% tp.date.now("YYYY-MM-DD HH:mm:ss") %>
// Output: 2025-12-14 15:30:45

<% tp.date.now("MMMM DD, YYYY") %>
// Output: December 14, 2025

<% tp.date.now("ddd, MMM D [at] h:mm A") %>
// Output: Sat, Dec 14 at 3:30 PM
```

---

### `tp.date.today(format?)`

Returns today's date (alias for `tp.date.now()`).

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted date

**Examples:**
```javascript
<% tp.date.today() %>
// Output: 2025-12-14

<% tp.date.today("dddd, MMMM DD, YYYY") %>
// Output: Saturday, December 14, 2025
```

---

### `tp.date.tomorrow(format?)`

Returns tomorrow's date.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted date

**Examples:**
```javascript
<% tp.date.tomorrow() %>
// Output: 2025-12-15

<% tp.date.tomorrow("MM/DD/YYYY") %>
// Output: 12/15/2025
```

---

### `tp.date.yesterday(format?)`

Returns yesterday's date.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted date

**Examples:**
```javascript
<% tp.date.yesterday() %>
// Output: 2025-12-13

<% tp.date.yesterday("YYYY-MM-DD") %>
// Output: 2025-12-13
```

---

### `tp.date.weekday(format?, offset?)`

Returns a specific weekday relative to today.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`
- `offset` (number, optional): Day offset (0=Sunday, 1=Monday, ..., 6=Saturday). Default: `0`

**Returns:** `string` - Formatted date

**Examples:**
```javascript
<% tp.date.weekday("YYYY-MM-DD", 1) %>
// Output: Next Monday's date

<% tp.date.weekday("dddd, MMMM DD", 5) %>
// Output: Next Friday, December 20

<% tp.date.weekday("YYYY-MM-DD", 0) %>
// Output: Next Sunday's date
```

---

### Date Format Patterns

| Pattern | Description | Example |
|---------|-------------|---------|
| **Year** | | |
| `YYYY` | 4-digit year | 2025 |
| `YY` | 2-digit year | 25 |
| **Month** | | |
| `MMMM` | Full month name | December |
| `MMM` | Short month name | Dec |
| `MM` | 2-digit month (01-12) | 12 |
| `M` | Month (1-12) | 12 |
| **Day** | | |
| `DD` | 2-digit day (01-31) | 14 |


### `tp.file.title`

Returns the file name without extension.

**Parameters:** None

**Returns:** `string` - File title

**Examples:**
```javascript
// File: my-document.md
<% tp.file.title %>
// Output: my-document

// File: project-notes.txt
<% tp.file.title %>
// Output: project-notes
```

---

### `tp.file.name`

Returns the full file name with extension.

**Parameters:** None

**Returns:** `string` - File name

**Examples:**
```javascript
// File: my-document.md
<% tp.file.name %>
// Output: my-document.md

// File: README.txt
<% tp.file.name %>
// Output: README.txt
```

---

### `tp.file.path(relative?)`

Returns the file path.

**Parameters:**
- `relative` (boolean, optional): If true, returns relative path. Default: `false` (absolute path)

**Returns:** `string` - File path

**Examples:**
```javascript
<% tp.file.path() %>
// Output: /Users/john/Documents/my-document.md

<% tp.file.path(true) %>
// Output: Documents/my-document.md
```

---

### `tp.file.folder()`

Returns the parent folder name.

**Parameters:** None

**Returns:** `string` - Folder name

**Examples:**
```javascript
// File: /Users/john/Documents/my-document.md
<% tp.file.folder() %>
// Output: Documents

// File: /Projects/MyApp/README.md
<% tp.file.folder() %>
// Output: MyApp
```

---

### `tp.file.content`

Returns the entire file content.

**Parameters:** None

**Returns:** `string` - File content

**Examples:**
```javascript
<%*
let content = tp.file.content
let lineCount = content.split("\n").length
tR += `This file has ${lineCount} lines`
%>
```

---

### `tp.file.selection`

Returns the currently selected text in the editor.

**Parameters:** None

**Returns:** `string` - Selected text (empty if no selection)

**Examples:**
```javascript
<% tp.file.selection %>
// Output: (selected text)

<%*
let selected = tp.file.selection
if (selected) {
    tR += `You selected: ${selected}`
} else {
    tR += "No text selected"
}
%>
```

---

### `tp.file.tags`

Returns tags from frontmatter or content.

**Parameters:** None

**Returns:** `string` - Comma-separated tags

**Examples:**
```markdown
---
tags: ["project", "important", "2025"]
---

<% tp.file.tags %>
// Output: project, important, 2025
```

---

### `tp.file.creation_date(format?)`

Returns the file creation date.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted creation date

**Examples:**
```javascript
<% tp.file.creation_date() %>
// Output: 2025-12-14

<% tp.file.creation_date("MMMM DD, YYYY") %>
// Output: December 14, 2025
```

---

### `tp.file.last_modified_date(format?)`

Returns the file's last modified date.

**Parameters:**
- `format` (string, optional): Date format pattern. Default: `"YYYY-MM-DD"`

**Returns:** `string` - Formatted modification date

**Examples:**
```javascript
<% tp.file.last_modified_date() %>
// Output: 2025-12-14

<% tp.file.last_modified_date("YYYY-MM-DD HH:mm") %>
// Output: 2025-12-14 15:30
```

---

## Frontmatter Module

**Namespace**: `tp.frontmatter`

Provides access to YAML frontmatter variables.

### Accessing Frontmatter

Access any frontmatter variable using dot notation.

**Syntax:**
```javascript
<% tp.frontmatter.variableName %>
<% tp.frontmatter.nested.property %>
```

**Examples:**

**Simple frontmatter:**
```markdown
---
title: "My Document"
author: "John Doe"
date: 2025-12-14
---

<% tp.frontmatter.title %>
// Output: My Document

<% tp.frontmatter.author %>
// Output: John Doe
```

**Nested frontmatter:**
```markdown
---
project:
  name: "Templater"
  version: "1.0.0"
  author:
    name: "John Doe"
    email: "john@example.com"
---

<% tp.frontmatter.project.name %>
// Output: Templater

<% tp.frontmatter.project.author.email %>
// Output: john@example.com
```

---

## System Module

**Namespace**: `tp.system`

Provides user interaction and system functions.

### `tp.system.prompt(message, defaultValue?)`

Shows an input dialog to the user.

**Parameters:**
- `message` (string): The prompt message to display
- `defaultValue` (string, optional): Default value in the input field

**Returns:** `string` - User input

**Examples:**
```javascript
<%*
let name = await tp.system.prompt("Enter your name")
tR += `Hello, ${name}!`
%>

<%*
let projectName = await tp.system.prompt("Project name", "My Project")
tR += `# ${projectName}`
%>
```

---

### `tp.system.suggester(items, values?)`

Shows a selection dialog with a list of options.

**Parameters:**
- `items` (array): Array of strings to display in the list
- `values` (array, optional): Array of values to return (same length as items). If omitted, returns the selected item.

**Returns:** `any` - Selected value

**Examples:**
```javascript
<%*
// Simple suggester (returns selected item)
let priority = await tp.system.suggester(["High", "Medium", "Low"])
tR += `Priority: ${priority}`
%>

<%*
// Suggester with custom values
let priority = await tp.system.suggester(
    ["🔴 High Priority", "🟡 Medium Priority", "🟢 Low Priority"],
    ["high", "medium", "low"]
)
tR += `Selected: ${priority}`  // Output: "high", "medium", or "low"
%>
```

---

### `tp.system.clipboard()`

Returns the current clipboard content.

**Parameters:** None

**Returns:** `string` - Clipboard text

**Examples:**
```javascript
<% tp.system.clipboard() %>
// Output: (clipboard content)

<%*
let clipboardText = tp.system.clipboard()
tR += `You copied: ${clipboardText}`
%>
```

---

## Web Module

**Namespace**: `tp.web`

Provides HTTP request functionality for fetching data from APIs.

### `tp.web.request(url, jsonPath?)`

Makes an HTTP GET request to the specified URL.

**Parameters:**
- `url` (string): The URL to fetch
- `jsonPath` (string, optional): Dot-notation path to extract from JSON response

**Returns:** `string` - Response body or extracted JSON value

**Examples:**
```javascript
<% tp.web.request("https://api.example.com/data") %>
// Output: (full response body)

<% tp.web.request("https://api.example.com/user", "name") %>
// Output: (value of "name" field from JSON response)

<% tp.web.request("https://api.example.com/user", "address.city") %>
// Output: (nested value from JSON response)
```

---

### `tp.web.daily_quote()`

Returns an inspirational quote of the day.

**Parameters:** None

**Returns:** `string` - Quote text with attribution

**Example:**
```javascript
<% tp.web.daily_quote() %>
// Output: "The only way to do great work is to love what you do." - Steve Jobs
```

---

### `tp.web.random_picture(query?, size?)`

Returns a URL to a random image.

**Parameters:**
- `query` (string, optional): Search term for the image. Default: `"nature"`
- `size` (string, optional): Image dimensions. Default: `"600x400"`

**Returns:** `string` - Image URL

**Examples:**
```javascript
<% tp.web.random_picture() %>
// Output: https://source.unsplash.com/600x400/?nature

<% tp.web.random_picture("mountains", "800x600") %>
// Output: https://source.unsplash.com/800x600/?mountains
```

---

## Obsidian Module

**Namespace**: `tp.obsidian`

Provides compatibility with Obsidian Templater scripts. These functions map to equivalent functionality in other modules.

### `tp.obsidian.request(options)`

Makes an HTTP GET request. This is an alias for `tp.web.request()` for Obsidian compatibility.

**Parameters:**
- `options` (object or string): Either an object with a `url` property, or a URL string directly

**Returns:** `string` - Response body

**Examples:**
```javascript
// Object literal syntax (Obsidian-style)
<% tp.obsidian.request({ url: "https://api.example.com/data" }) %>

// String URL syntax
<% tp.obsidian.request("https://api.example.com/data") %>

// In script blocks
<%*
const response = tp.obsidian.request({ url: "https://api.example.com/data" })
tR += response
%>
```

---

## Config Module

**Namespace**: `tp.config`

Provides runtime configuration information.

### `tp.config.active_file`

Returns the path of the currently active file.

**Type:** `string`

**Example:**
```javascript
<% tp.config.active_file %>
// Output: /Users/john/Documents/my-document.md
```

---

### `tp.config.template_file`

Returns the path of the template file being executed.

**Type:** `string`

**Example:**
```javascript
<% tp.config.template_file %>
// Output: /Users/john/Templates/daily-note.md
```

---

### `tp.config.target_file`

Returns the path of the target file for the template.

**Type:** `string`

**Example:**
```javascript
<% tp.config.target_file %>
// Output: /Users/john/Documents/new-note.md
```

---

### `tp.config.run_mode`

Returns how Templater was launched.

**Type:** `string`

**Possible values:**
- `CREATE_NEW_FROM_TEMPLATE`
- `APPEND_ACTIVE_FILE`
- `OVERWRITE_FILE`
- `OVERWRITE_ACTIVE_FILE`
- `DYNAMIC_PROCESSOR`
- `STARTUP_TEMPLATE`

**Example:**
```javascript
<% tp.config.run_mode %>
// Output: OVERWRITE_ACTIVE_FILE
```

---

## Script Engine

The script engine supports JavaScript-like syntax in execution commands (`<%* %>`).

### Variables

Declare variables using `let`, `const`, or `var`:

```javascript
<%*
let name = "John"
const age = 30
var city = "New York"
%>
```

### Operators

**Comparison:**
- `==` - Equal
- `!=` - Not equal
- `<` - Less than
- `>` - Greater than
- `<=` - Less than or equal
- `>=` - Greater than or equal

**String:**
- `+` - Concatenation

**Example:**
```javascript
<%*
let x = 10
let y = 20

if (x < y) {
    tR += "x is less than y"
}
%>
```

### Control Flow

**If/Else:**
```javascript
<%*
let hour = new Date().getHours()

if (hour < 12) {
    tR += "Good morning"
} else if (hour < 18) {
    tR += "Good afternoon"
} else {
    tR += "Good evening"
}
%>
```

**For Loops:**
```javascript
<%*
for (let i = 1; i <= 5; i++) {
    tR += `Item ${i}\n`
}
%>
```

**For-of Loops:**
```javascript
<%*
const items = ["Apple", "Banana", "Cherry"]
for (const item of items) {
    tR += `- ${item}\n`
}
%>
```

**Try/Catch:**
```javascript
<%*
try {
    let result = tp.web.request("https://api.example.com/data")
    tR += result
} catch (e) {
    tR += "Error: " + e
} finally {
    tR += "\n---"
}
%>
```

### Arrow Functions

Arrow functions are supported for callbacks:

```javascript
<%*
const nums = [1, 2, 3, 4, 5]

// Single parameter (no parentheses needed)
const doubled = nums.map(x => x * 2)
// Result: [2, 4, 6, 8, 10]

// With filter
const evens = nums.filter(x => x % 2 == 0)
// Result: [2, 4]

// Chaining
const result = nums.filter(x => x > 2).map(x => x * 10)
// Result: [30, 40, 50]

tR += doubled.join(", ")
%>
```

### String Operations

**Concatenation:**
```javascript
<%*
let firstName = "John"
let lastName = "Doe"
let fullName = firstName + " " + lastName
%>
```

**Template Literals:**
```javascript
<%*
let name = "Alice"
let age = 25
tR += `My name is ${name} and I'm ${age} years old`
%>
```

**Escape Sequences:**
- `\n` - Newline
- `\t` - Tab

```javascript
<%*
tR += `Line 1\nLine 2\nLine 3`
%>
```

**String Methods:**

| Method | Description | Example |
|--------|-------------|---------|
| `.trim()` | Remove leading/trailing whitespace | `"  hello  ".trim()` → `"hello"` |
| `.split(sep)` | Split string into array | `"a,b,c".split(",")` → `["a","b","c"]` |
| `.join(sep)` | Join array into string | `["a","b"].join("-")` → `"a-b"` |
| `.padStart(len, char)` | Pad start of string | `"5".padStart(3, "0")` → `"005"` |
| `.substring(start, end)` | Extract substring | `"hello".substring(0, 3)` → `"hel"` |
| `.replaceAll(old, new)` | Replace all occurrences | `"a-b-c".replaceAll("-", "_")` → `"a_b_c"` |

```javascript
<%*
let text = "  Hello, World!  "
tR += text.trim()                      // "Hello, World!"

let parts = "apple,banana,cherry".split(",")
tR += parts.join(" | ")                // "apple | banana | cherry"

let num = "42"
tR += num.padStart(5, "0")             // "00042"

let str = "Hello World"
tR += str.substring(0, 5)              // "Hello"
tR += str.replaceAll(" ", "_")         // "Hello_World"
%>
```

### Object Literals

Create objects using `{ key: value }` syntax:

```javascript
<%*
const options = { url: "https://api.example.com", method: "GET" }
tR += options.url      // "https://api.example.com"
tR += options.method   // "GET"

// Useful for Obsidian-compatible API calls
const response = tp.obsidian.request({ url: "https://api.example.com/data" })
%>
```

### The `tR` Variable

`tR` (template Result) is a special variable that accumulates output:

**Operations:**
```javascript
<%*
tR = "text"      // Replace output
tR += "text"     // Append to output
let x = tR       // Read current output
%>
```

**Example:**
```javascript
<%*
tR = ""  // Start fresh
tR += "# Header\n\n"
tR += "Paragraph 1\n\n"
tR += "Paragraph 2"
%>
```

### Function Calls

Call module functions within scripts:

```javascript
<%*
let today = tp.date.now("YYYY-MM-DD")
let fileName = tp.file.name
let author = await tp.system.prompt("Your name")

tR += `# ${fileName}\n\n`
tR += `**Date**: ${today}\n`
tR += `**Author**: ${author}\n`
%>
```

### Await Keyword

Use `await` with system functions (for Obsidian compatibility):

```javascript
<%*
let name = await tp.system.prompt("Enter name")
let choice = await tp.system.suggester(["A", "B", "C"])
%>
```

**Note:** The `await` keyword is recognized but execution is synchronous.

---

## Complete Example

Here's a comprehensive example using multiple modules:

```javascript
<%*
// Gather user input
let projectName = await tp.system.prompt("Project name")
let projectType = await tp.system.suggester(
    ["Web App", "Mobile App", "Desktop App", "Library"],
    ["web", "mobile", "desktop", "library"]
)
let author = await tp.system.prompt("Author name")

// Get dates
let today = tp.date.now("YYYY-MM-DD")
let todayFormatted = tp.date.now("MMMM DD, YYYY")

// Get file info
let fileName = tp.file.name
let filePath = tp.file.path()

// Build output
tR += `---\n`
tR += `title: "${projectName}"\n`
tR += `type: ${projectType}\n`
tR += `author: "${author}"\n`
tR += `created: ${today}\n`
tR += `---\n\n`

tR += `# ${projectName}\n\n`
tR += `**Type**: ${projectType}\n`
tR += `**Author**: ${author}\n`
tR += `**Created**: ${todayFormatted}\n`
tR += `**File**: ${fileName}\n`
tR += `**Path**: ${filePath}\n\n`

tR += `## Overview\n\n`
tR += `\n\n`

tR += `## Features\n\n`
for (let i = 1; i <= 3; i++) {
    tR += `- Feature ${i}\n`
}

tR += `\n## Installation\n\n`
tR += `\`\`\`bash\n`
tR += `# Installation instructions\n`
tR += `\`\`\`\n\n`

tR += `## Usage\n\n`
tR += `\`\`\`\n`
tR += `// Usage example\n`
tR += `\`\`\`\n\n`

tR += `---\n`
tR += `Last updated: ${tp.date.now("YYYY-MM-DD HH:mm")}\n`
%>
```

---

## Error Handling

If a command fails, an error message is inserted:

```javascript
<% tp.invalid.command() %>
// Output: ERROR: Unknown module 'invalid'

<% tp.date.invalidFunction() %>
// Output: ERROR: Unknown function 'invalidFunction'
```

---

## Best Practices

1. **Use `await` with system functions** for consistency with Obsidian Templater
2. **Use template literals** for cleaner string formatting
3. **Initialize `tR`** at the start of execution blocks if needed
4. **Check for null/undefined** when accessing frontmatter
5. **Use meaningful variable names** for readability