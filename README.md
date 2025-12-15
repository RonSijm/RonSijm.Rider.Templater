# Templater

A template engine plugin for JetBrains IDEs with syntax compatible with Obsidian's Templater plugin.

Built for Rider and IntelliJ IDEA, but should work in any JetBrains IDE on the same platform core.

It's not fully compatible with Templater yet, but it's a start. Most functionality is there, but Obsidian allows you to start a full DOM and Javascript parser.

I implemented the basic functionality natively, but I don't know if I want to do that with the DOM...

---

## Installation

### Install from Marketplace

1. In your IDE: **File → Settings → Plugins → Marketplace**
2. Search for "Templater" and click **Install**
3. Restart the IDE
4. [Or find directly in Marketplace url](https://plugins.jetbrains.com/plugin/29385-templater)

### From Built Plugin

1. Download `rider-templater-1.0.0.zip` or `intellij-templater-1.0.0.zip` from releases
2. In your IDE: **File → Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the `.zip` file and restart

### Build from Source

```bash
git clone https://github.com/RonSijm/RonSijm.Rider.Templater.git
cd RonSijm.Rider.Templater

# Build for Rider
.\gradlew.bat buildPlugin -Pvariant=rider

# Build for IntelliJ IDEA
.\gradlew.bat buildPlugin -Pvariant=intellij
```

Output: `build/distributions/rider-templater-1.0.0.zip` or `intellij-templater-1.0.0.zip`

---

## Quick Start

1. Create a markdown file with template syntax
2. Press **Alt+R** (Rider) or **Alt+Shift+R** (IntelliJ) to execute

### Execute Selection Only

Select a specific template block (e.g., a single `<%* %>` block) and execute to process only that selection. The rest of the document remains unchanged.

```markdown
# Meeting Notes - <% tp.date.now("YYYY-MM-DD") %>

**Attendee:** <% await tp.system.prompt("Who attended?") %>

## Action Items
<%*
for (let i = 1; i <= 3; i++) {
    tR += "- [ ] Task " + i + "\n"
}
%>
```

---

## Template Syntax

### Interpolation: `<% %>`

Outputs the result directly:

```markdown
<% tp.date.now("YYYY-MM-DD") %>
<% tp.file.title %>
<% tp.frontmatter.author %>
```

### Execution: `<%* %>`

Runs code. Use `tR` to accumulate output:

```javascript
<%*
let name = await tp.system.prompt("Your name")
tR += "Hello, " + name + "!"
%>
```

### Whitespace Control

Add `-` to trim whitespace:

```markdown
<%- tp.date.now() %>   // Trim left
<% tp.date.now() -%>   // Trim right
<%- tp.date.now() -%>  // Trim both
```

---

## Modules

### Date (`tp.date`)

```javascript
<% tp.date.now("YYYY-MM-DD HH:mm:ss") %>
<% tp.date.today("MMMM DD, YYYY") %>
<% tp.date.tomorrow("ddd, MMM D") %>
<% tp.date.yesterday("DD/MM/YYYY") %>
<% tp.date.weekday("YYYY-MM-DD", 1) %>  // Next Monday
```

### File (`tp.file`)

| Command | Description |
|---------|-------------|
| `tp.file.title` | File name without extension |
| `tp.file.name` | Full file name |
| `tp.file.path()` | File path |
| `tp.file.folder()` | Parent folder name |
| `tp.file.content` | File content |
| `tp.file.selection` | Selected text |
| `tp.file.tags` | Tags from frontmatter |
| `tp.file.creation_date(format)` | File creation date |
| `tp.file.last_modified_date(format)` | Last modified date |

### Frontmatter (`tp.frontmatter`)

Access YAML frontmatter at the top of your file:

```yaml
---
title: "My Document"
author:
  name: "John"
  email: "john@example.com"
---
```

```javascript
<% tp.frontmatter.title %>           // "My Document"
<% tp.frontmatter.author.name %>     // "John"
```

### System (`tp.system`)

| Command | Description |
|---------|-------------|
| `tp.system.prompt(message, default)` | Input dialog, returns user input |
| `tp.system.suggester(items, values)` | Dropdown selection, returns selected value |
| `tp.system.clipboard()` | Returns clipboard content |

```javascript
<%* let name = await tp.system.prompt("Enter name", "Default") %>
<%* let choice = await tp.system.suggester(["Option A", "Option B"], ["a", "b"]) %>
<% tp.system.clipboard() %>
```

### Web (`tp.web`)

```javascript
<% tp.web.request("https://api.example.com/data") %>
<% tp.web.request("https://api.example.com/data", "result.value") %>  // JSON path
<% tp.web.daily_quote() %>
<% tp.web.random_picture("nature", "200x200") %>
```

### Obsidian Compatibility (`tp.obsidian`)

For compatibility with Obsidian Templater scripts:

```javascript
<% tp.obsidian.request({ url: "https://api.example.com/data" }) %>
<% tp.obsidian.request("https://api.example.com/data") %>
```

### Config (`tp.config`)

```javascript
<% tp.config.active_file %>
<% tp.config.template_file %>
<% tp.config.run_mode %>
```

---

## Scripting

Execution blocks support JavaScript-like syntax.

### Variables

```javascript
<%*
let name = "John"
const age = 30
var city = "New York"
tR += name + " is " + age
%>
```

### Template Literals

```javascript
<%*
let name = "Alice"
tR += `Hello, ${name}!`
%>
```

### For Loops

```javascript
<%*
for (let i = 1; i <= 5; i++) {
    tR += "- Item " + i + "\n"
}
%>
```

### For-of Loops

```javascript
<%*
const items = ["Apple", "Banana", "Cherry"]
for (const item of items) {
    tR += "- " + item + "\n"
}
%>
```

### Arrow Functions

```javascript
<%*
const nums = [1, 2, 3, 4, 5]
const doubled = nums.map(x => x * 2)
const filtered = nums.filter(x => x > 2)
tR += doubled.join(", ")
%>
```

### If/Else

```javascript
<%*
let score = 85
if (score >= 90) {
    tR += "Grade: A"
} else if (score >= 80) {
    tR += "Grade: B"
} else {
    tR += "Grade: C"
}
%>
```

Supported operators: `==`, `!=`, `<`, `>`, `<=`, `>=`

### Try/Catch

```javascript
<%*
try {
    let result = tp.web.request("https://api.example.com/data")
    tR += result
} catch (e) {
    tR += "Failed to fetch data"
} finally {
    tR += "\n---"
}
%>
```

### String Methods

```javascript
<%*
let text = "  Hello, World!  "
tR += text.trim()                    // "Hello, World!"
tR += text.split(",").join(" -")     // "  Hello - World!  "
tR += "abc".padStart(5, "0")         // "00abc"
tR += "Hello".substring(0, 3)        // "Hel"
tR += "foo bar".replaceAll(" ", "_") // "foo_bar"
%>
```

### Object Literals

```javascript
<%*
const options = { url: "https://api.example.com", method: "GET" }
tR += options.url
%>
```

### The `tR` Variable

`tR` (template Result) accumulates output in execution blocks:

```javascript
<%*
tR = ""           // Reset
tR += "Line 1\n"  // Append
tR += "Line 2\n"
%>
```

---

## Examples

### Daily Note

```markdown
---
date: <% tp.date.now("YYYY-MM-DD") %>
---

# <% tp.date.now("MMMM DD, YYYY") %>

## Tasks
- [ ]
- [ ]
- [ ]

## Notes


---
Created: <% tp.date.now("HH:mm") %>
```

### Meeting Notes

```javascript
<%*
let title = await tp.system.prompt("Meeting title")
let attendees = await tp.system.prompt("Attendees")
let date = tp.date.now("YYYY-MM-DD")

tR += `# ${title}\n\n`
tR += `**Date**: ${date}\n`
tR += `**Attendees**: ${attendees}\n\n`
tR += `## Agenda\n\n1. \n2. \n3. \n\n`
tR += `## Action Items\n\n- [ ] \n- [ ] \n`
%>
```

### Dynamic List

```javascript
<%*
let count = await tp.system.prompt("How many items?", "5")
let prefix = await tp.system.suggester(["Task", "Note", "Idea"], ["- [ ]", "-", "*"])

for (let i = 1; i <= parseInt(count); i++) {
    tR += prefix + " Item " + i + "\n"
}
%>
```

---

## Troubleshooting

**Template not executing**
- Check plugin is installed: Settings → Plugins
- Try right-click → Execute Template
- Check if shortcut conflicts: Settings → Keymap

**Syntax errors**
- Use `<% %>` for output, `<%* %>` for code
- Check balanced quotes and parentheses
- Use `tp.` prefix for all functions

**Frontmatter not found**
- Must be at the very top of the file
- Must be enclosed in `---` markers
- Variable names are case-sensitive

**Date format wrong**
- Use `YYYY` for year (not `yyyy`)
- Use `MM` for month, `DD` for day
- Use `HH:mm` for 24-hour time

---

## Development

```bash
.\gradlew.bat buildPlugin      # Build
.\gradlew.bat unitTest         # Run tests
.\gradlew.bat runIde           # Run in IDE
```

---

## Documentation

See the [docs](docs/) folder for more detailed documentation:

- **[Architecture](docs/Architecture.md)** - Internal architecture, code structure, and how to add new commands
- **[API Reference](docs/api_reference.md)** - Complete API reference for all modules and functions
- **[Examples](docs/examples/)** - Example templates demonstrating various features:
  - [Date Functions](docs/examples/01-date-functions.md)
  - [File Functions](docs/examples/02-file-functions.md)
  - [Frontmatter Functions](docs/examples/03-frontmatter-functions.md)
  - [System Functions](docs/examples/04-system-functions.md)
  - [Script Execution](docs/examples/05-script-execution.md)
  - [Whitespace Control](docs/examples/06-whitespace-control.md)
  - [Config Functions](docs/examples/07-config-functions.md)
  - [Complete Example](docs/examples/08-complete-example.md)
  - [Parallel Execution](docs/examples/10-parallel-execution.md)

---

## License

AGPL-3.0 License (same as Obsidian's Templater plugin)

## Acknowledgments

Inspired by [Templater](https://github.com/SilentVoid13/Templater) for Obsidian