# 4 - Numbered Callout with Hashtag Support

Advanced callout creator with numbered selection for quick keyboard access. Each callout alias is listed separately for precise control. Supports embedded hashtags in type definitions that are automatically extracted and appended to the output.

**Source:** https://github.com/SilentVoid13/Templater/discussions/922#discussioncomment-13540662

```
<%*
const callouts = {
//  Callout name   |  Prompt Name     |  UI Icon Description

    // Red - Critical/Error group
    "bug":            "🟥 🪳 Bug #hello",        // Bug icon
    "danger":         "🟥 ⚡️ Danger",     // Lightning Bolt icon
    "error":          "🟥 ⚡️ Error",      // Lightning Bolt icon
    "fail":           "🟥 ❌ Fail",       // 'X' mark icon
    "failure":        "🟥 ❌ Failure",    // 'X' mark icon
    "missing":        "🟥 ❌ Missing",    // 'X' mark icon
    
    // Orange - Warning group
    "attention":      "🟧 ⚠️ Attention",  // Exclamation Sign icon
    "caution":        "🟧 ⚠️ Caution",    // Exclamation Sign icon
    "warning":        "🟧 ⚠️ Warning",    // Exclamation Sign icon
    "help":           "🟧 ❓ Help",       // Question Mark in Circle icon
    "faq":            "🟧 ❓ FAQ",        // Question Mark in Circle icon
    "question":       "🟧 ❓ Question",   // Question Mark in Circle icon
    
    // Green - Success group
    "done":           "🟩 ✅ Done",       // Green Checkmark icon
    "check":          "🟩 ✅ Check",      // Green Checkmark icon
    "success":        "🟩 ✅ Success",    // Green Checkmark icon
    
    // Blue - Information group
    "info":           "🟦 ⓘ Info",       // 'i' in Circle icon
    "note":           "🟦 ✏️ Note",       // Pencil icon
    "abstract":       "🟦 📋 Abstract",   // Clipboard icon 
    "summary":        "🟦 📋 Summary",    // Clipboard icon  
    "tldr":           "🟦 📋 TL;DR;",     // Clipboard icon  
    "example":        "🟦 📑 Example",    // Outline icon (ish so a folder?)
    "hint":           "🟦 🔥 Hint",       // Flame icon
    "important":      "🟦 🔥 Important",  // Flame icon
    "tip":            "🟦 🔥 Tip",        // Flame icon
    "todo":           "🟦 ✅ Todo",       // Checkmark in Circle icon
    
    // White - Quotes
    "cite":           "⬜️ ⍘ Cite",        // Quotation Mark icon
    "quote":          "⬜️ ⍘ Quote",       // Quotation Mark icon
    
    // Custom types (via Callout Manager)
};

const typeNames = [];
const typeLabels = [];

// Push all keys into `typeNames`
// Push all values into `typeLabels`
// Only values before the 1st instance of '#' is pushed into the `typeLabels` array
Object.keys(callouts).forEach((key, index) => {
  typeNames.push(key);
  let label = callouts[key].split('#')[0].trim();
  typeLabels.push(`${index + 1}. ${label}`);
});

// User chooses from list comprised of `typeNames` + `typeLabels`
let calloutType = await tp.system.suggester(
    typeLabels,
    typeNames,
    false,
    "Select callout type (use numbers 1-" + typeLabels.length + " to select)"
);

// Stop here when the prompt was cancelled (ESC).
if (!calloutType) {
    return;
}

// Get Hashtags found in `callouts` as per user selected `calloutType`
let hashTags = callouts[calloutType]
  .match(/#[A-Za-z0-9\-]+/g)?.join(' ')
  .trim() || '';

// Ask user for callout title
let title = await tp.system.prompt("Callout Header:");
_%>

> [!<% calloutType %>]+ <% title %> <% hashTags %> 
> <%* tp.file.cursor() %>
```