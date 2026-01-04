# Templater Standalone Application

A standalone desktop application for debugging and testing Templater templates with a full-featured debugger.

## Features

- ✅ **IntelliJ Darcula Theme** - Uses FlatLaf for IntelliJ-like appearance
- ✅ **File Browser** - Navigate and open template files
- ✅ **Syntax Highlighting** - Markdown and code syntax highlighting with RSyntaxTextArea
- ✅ **Full Debugger** - Step through template execution with breakpoints
- ✅ **Variable Inspector** - View and modify variables during debugging
- ✅ **Execution Trace** - See the complete execution flow
- ✅ **No IntelliJ Required** - Runs as a standalone JAR

## Building

```bash
./gradlew :cli:build
```

This creates a fat JAR at `build/cli/templater.jar`

## Running

```bash
java -jar build/cli/templater.jar
```

## Usage

### 1. Open a Folder
- Click **File > Open Folder** or use the toolbar button
- Navigate to your templates directory

### 2. Open a Template File
- Click on a file in the file tree
- Or use **File > Open File**

### 3. Set Breakpoints
- Click **Toggle Breakpoint** button or use the menu
- Breakpoints are set at the current cursor line

### 4. Start Debugging
- Click **Debug > Start Debugging**
- The debugger will pause at the first breakpoint or first statement

### 5. Debug Controls
- **Continue (F9)** - Run until next breakpoint
- **Step Into (F7)** - Execute next statement and pause
- **Step Over (F8)** - Execute next statement at same level
- **Step Out (Shift+F8)** - Execute until exiting current scope
- **Stop (Ctrl+F2)** - Stop debugging

### 6. Inspect Variables
- View current variables in the **Variables** panel (right side)
- Variables update automatically when paused
- You can edit variable values during debugging

### 7. View Execution Trace
- See the complete execution flow in the **Execution Trace** panel (bottom)
- Shows all executed statements with line numbers

## UI Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Menu Bar: File | Debug | Help                               │
├──────────┬──────────────────────────────────┬───────────────┤
│          │                                  │               │
│  File    │         Editor                   │   Variables   │
│  Tree    │    (Syntax Highlighting)         │               │
│          │                                  │               │
│          ├──────────────────────────────────┤               │
│          │                                  │               │
│          │    Debugger Controls             │               │
│          │    Execution Trace               │               │
│          │                                  │               │
└──────────┴──────────────────────────────────┴───────────────┘
```

## Technology Stack

- **UI Framework**: Swing with FlatLaf (IntelliJ Darcula theme)
- **Syntax Highlighting**: RSyntaxTextArea
- **Markdown Rendering**: CommonMark
- **Core Engine**: Templater Core (shared with plugin)
- **Build**: Gradle with Shadow plugin for fat JAR

## Differences from Plugin

| Feature | Plugin | Standalone |
|---------|--------|------------|
| **Platform** | IntelliJ IDEA | Any Java 21+ |
| **UI** | IntelliJ Platform | FlatLaf + Swing |
| **File Access** | VirtualFile | java.io.File |
| **Editor** | IntelliJ Editor | RSyntaxTextArea |
| **Debugger** | ✅ Full | ✅ Full (same engine) |
| **Templates** | ✅ Full | ✅ Full (same engine) |

## Development

### Project Structure

```
src/cli/
├── src/main/kotlin/ronsijm/templater/
│   ├── standalone/
│   │   ├── TemplaterApp.kt          # Main entry point
│   │   ├── ui/
│   │   │   ├── MainWindow.kt        # Main application window
│   │   │   ├── FileTreePanel.kt     # File browser
│   │   │   ├── EditorPanel.kt       # Code editor
│   │   │   ├── DebugPanel.kt        # Debugger controls
│   │   │   └── VariablesPanel.kt    # Variable inspector
│   │   └── debug/
│   │       └── StandaloneDebugSession.kt  # Debug session management
│   └── ...
└── build.gradle.kts
```

### Adding Features

The standalone app shares the core templating engine with the plugin (`project(":core")`), so any improvements to the core automatically benefit both.

To add UI features:
1. Create new panel in `ui/` package
2. Add to `MainWindow.kt` layout
3. Wire up event listeners

## Troubleshooting

### Application won't start
- Ensure Java 21+ is installed: `java -version`
- Check the JAR exists: `ls build/cli/templater.jar`

### Debugger not working
- Make sure you have a file open
- Set at least one breakpoint or the debugger will run to completion
- Check the console for error messages

### UI looks wrong
- FlatLaf should automatically detect your system theme
- The app uses Darcula (dark) theme by default
- To change, modify `TemplaterApp.kt` and use `FlatIntelliJLaf.setup()` for light theme

## License

Same as the main Templater project.

