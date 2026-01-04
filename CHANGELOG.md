# Changelog

All notable changes to the RonSijm.Rider.Templater plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-01-04

### 🎯 Major Features

#### **Standalone CLI Application**
- **NEW**: Complete standalone desktop application with modern Swing UI
  - Full-featured file tree browser with workspace management
  - Tabbed editor with syntax highlighting for template blocks
  - Live preview panel showing rendered output
  - Recent folders tracking for quick access
  - File association support for `.mmd` files
  - Detachable panels for multi-monitor workflows

#### **Advanced Debugging System**
- **NEW**: Interactive step-by-step debugger for template execution
  - Set breakpoints by clicking in the editor gutter
  - Step through template execution line-by-line
  - Inspect variables at each execution step
  - View call stack and execution trace
  - Debug tool window with step controls (Continue, Step Over, Step Into, Step Out)
  - Variables tool window showing current scope
  - Execution statistics and performance metrics

#### **Control Flow Visualization**
- **NEW**: Interactive Mermaid diagram generation
  - Automatic control flow graph generation from templates
  - Visual representation of loops, conditionals, and function calls
  - Node highlighting during debugging
  - Export diagrams to Mermaid format
  - Configurable node colors and styles in settings
  - Support for parallel execution visualization

#### **Algorithm Visualization**
- **NEW**: Real-time visualization of data structure changes
  - Array visualization with element highlighting
  - State change detection during execution
  - Visual metadata for sorting algorithms
  - Step-by-step animation support
  - Includes 9 sorting algorithm examples (Bubble, Quick, Merge, Heap, Insertion, Selection, Counting, Bucket, Radix)

### 🚀 IDE Integration Enhancements

#### **Editor Features**
- **NEW**: Run line markers - Execute templates directly from the editor
- **NEW**: Breakpoint gutter icons with click handlers
- **NEW**: Template block syntax highlighting
- **NEW**: Code completion for template functions
- **NEW**: Context-aware completion suggestions
- **NEW**: Completion context analyzer for intelligent suggestions

#### **Tool Windows**
- **NEW**: Debug tool window with execution controls
- **NEW**: Variables tool window for inspecting template state
- **NEW**: Control Flow diagram tool window
- **NEW**: Docking support for flexible layout management

#### **Actions & Menu Items**
- **NEW**: "Debug Template" action for starting debug sessions
- **NEW**: "Execute Template" action with improved error handling
- **NEW**: "Run Selected Template" for partial execution
- **NEW**: "Show Control Flow" to visualize template structure

### 🔧 Core Engine Improvements

#### **Script Engine Enhancements**
- **NEW**: Bytecode compiler for improved performance
  - Expression caching for repeated evaluations
  - Peephole optimization for bytecode
  - Specialized opcodes for common operations
  - 3-5x performance improvement on complex scripts

#### **AST (Abstract Syntax Tree) Support**
- **NEW**: Full AST-based script engine
  - AST interpreter for advanced analysis
  - Constant folding visitor for optimization
  - Type checking visitor for validation
  - Variable collector for dependency analysis
  - Pretty print visitor for debugging

#### **Enhanced JavaScript Compatibility**
- **NEW**: Ternary operator support (`condition ? true : false`)
- **NEW**: `typeof` operator
- **NEW**: `new` expression for object construction
- **NEW**: Logical expression short-circuiting
- **NEW**: Property access evaluation
- **NEW**: Template literal improvements
- **NEW**: Arrow function enhancements
- **NEW**: Global functions (parseInt, parseFloat, isNaN, isFinite)
- **NEW**: Math object with full method support
- **NEW**: Date object with formatting methods
- **NEW**: Number methods (toFixed, toPrecision, toExponential)

#### **Parser Improvements**
- **NEW**: Structured concurrency for parallel execution
- **NEW**: Cancellation token support
- **NEW**: Progress reporting for long-running templates
- **NEW**: Improved error messages with location information
- **NEW**: Error collection and formatting
- **NEW**: Template validation with detailed error codes

### ⚙️ Settings & Configuration

#### **NEW Settings Panels**
- **General Settings**: Theme selection, font size, workspace preferences
- **Execution Settings**: Timeout configuration, parallel execution options
- **Mermaid Settings**: Node colors, styles, diagram layout options
- **Hotkeys Settings**: Customizable keyboard shortcuts
- **After Running Settings**: Post-execution actions

#### **Mermaid Customization**
- Configurable node colors for different block types
- Style presets (Default, Pastel, Vibrant, Monochrome)
- Custom CSS styling support
- Layout direction options

### 📦 Project Structure

#### **Multi-Module Architecture**
- **core**: Template engine and script execution
- **plugin**: JetBrains IDE integration
- **cli**: Standalone desktop application
- **common-ui**: Shared UI components
- **ksp-codegen**: Code generation for handlers

### 🧪 Testing & Quality

- **NEW**: 150+ new test files covering all major features
- **NEW**: Benchmark tests for performance validation
- **NEW**: Integration tests for debugging features
- **NEW**: UI tests for standalone application
- **NEW**: Detekt configuration for code quality
- Comprehensive test coverage for:
  - Breakpoint resolution and handling
  - Control flow graph generation
  - Debugging session management
  - Algorithm visualization
  - Script engine features
  - Bytecode compilation

### 📚 Documentation & Examples

#### **NEW Example Templates**
- **Debugging Examples**: Debugger showcase, variable editing, for loops
- **Sorting Algorithms**: 9 complete sorting algorithm implementations
- **Benchmarks**: Performance testing templates
  - Algorithmic benchmarks (Pi calculation, matrix multiplication)
  - Templating benchmarks (escape-heavy, frontmatter lookup, loop rendering)
- **Template Examples**: Callout templates with various styles

#### **Mermaid Diagrams**
- All examples now include `.mmd` files with control flow diagrams
- Visual documentation of template execution flow

### 🔨 Build & Development

- **NEW**: Separate build scripts for CLI and plugin
- **NEW**: Detekt integration for code quality
- **NEW**: Multi-module Gradle configuration
- **NEW**: GitHub Actions workflow improvements
- Version management centralized in `gradle.properties`

### 🐛 Bug Fixes

- Fixed cancellation handling in template execution
- Improved error messages for template validation
- Fixed parallel execution race conditions
- Corrected breakpoint resolution in nested blocks
- Fixed variable scope issues in debugging
- Improved file path validation

### ⚡ Performance

- 3-5x faster script execution with bytecode compiler
- Optimized expression evaluation with caching
- Reduced memory footprint in parallel execution
- Improved rendering performance for large templates

### 🔄 Breaking Changes

- Project structure reorganized into multi-module layout
- Settings moved from plugin-specific to shared configuration
- Some internal APIs refactored for better modularity

---

## [1.0.2] - 2025-12-17

### Features

#### **Handler System Improvements**
- **NEW**: Cancellable handler interface for user-initiated cancellation
  - Prompt handler now supports cancellation
  - Suggester handler supports cancellation
  - Multi-suggester handler supports cancellation
  - Proper "request cancelled" responses instead of null values

#### **Low-Level Parsing Functions**
- **NEW**: Enhanced script evaluator with better parsing capabilities
- **NEW**: Arithmetic evaluator for complex mathematical expressions
- **NEW**: Arrow function handler improvements
- **NEW**: Function call executor enhancements
- **NEW**: Literal parser for better type handling
- **NEW**: Template literal evaluator improvements

#### **Example Templates**
- **NEW**: Interactive callout templates
  - Basic interactive callout
  - Emoji-labeled callout with content
  - Interactive callout with fold options
  - Numbered callout with hashtag support

### Improvements

- Refactored handler metadata system for better extensibility
- Improved error handling in command execution
- Enhanced test coverage for handlers
- Better separation of concerns in handler architecture
- Improved mock services for testing

### Code Quality

- Reorganized test files into logical groups
  - Date handler tests
  - File handler tests
  - System handler tests
  - Web handler tests
- Added base test classes for common test functionality
- Improved test naming and organization

### Bug Fixes

- Fixed handler registration issues
- Corrected parameter parsing in various handlers
- Improved error messages for failed handler execution

---

## [1.0.1] - 2025-12-16

### Features

#### **Configurable Selection Parsing**
- **NEW**: "Parse selection only" setting in plugin configuration
  - When enabled: Only selected text is processed as template
  - When disabled: Entire file is processed (matches original Templater behavior)
  - Allows use of `<% tp.file.selection() %>` when disabled
  - Configurable via Settings → Tools → Templater

### IDE Integration

- **NEW**: Template execution action improvements
  - Better handling of text selection
  - Improved error reporting
  - Progress indicator support

### Documentation

- **NEW**: Visual documentation with before/after screenshots
  - Date function examples with screenshots
  - Frontmatter function examples with screenshots
- Improved README with configuration instructions
- Better explanation of selection parsing behavior

### Build & CI

- **NEW**: GitHub Actions workflow enhancements
  - Automated build verification
  - Test execution in CI
  - Artifact publishing

### Bug Fixes

- Fixed selection parsing behavior to match user expectations
- Corrected frontmatter example documentation
- Improved parallel template parser selection handling

---

## [1.0.0] - 2025-12-16 (Initial Release)

### 🎉 Initial Release

#### **Core Template Engine**
- Full template parsing and execution engine
- JavaScript-like scripting support within templates
- Frontmatter parsing and processing
- Parallel template execution for improved performance
- Template validation with detailed error messages

#### **Template Functions**

##### **Date Functions**
- `tp.date.now()` - Current date/time with formatting
- `tp.date.today()` - Today's date
- `tp.date.tomorrow()` - Tomorrow's date
- `tp.date.yesterday()` - Yesterday's date
- `tp.date.weekday()` - Current weekday with formatting

##### **File Functions**
- `tp.file.content()` - Get file content
- `tp.file.create_new()` - Create new file
- `tp.file.creation_date()` - Get file creation date
- `tp.file.cursor()` - Set cursor position
- `tp.file.cursor_append()` - Append at cursor
- `tp.file.exists()` - Check file existence
- `tp.file.find_tfile()` - Find template file
- `tp.file.folder()` - Get file folder
- `tp.file.include()` - Include another template
- `tp.file.last_modified_date()` - Get last modified date
- `tp.file.move()` - Move file
- `tp.file.name()` - Get file name
- `tp.file.path()` - Get file path
- `tp.file.rename()` - Rename file
- `tp.file.selection()` - Get selected text
- `tp.file.tags()` - Get file tags
- `tp.file.title()` - Get file title

##### **System Functions**
- `tp.system.clipboard()` - Access clipboard content
- `tp.system.prompt()` - Show input prompt
- `tp.system.suggester()` - Show suggestion picker
- `tp.system.multi_suggester()` - Show multi-select picker

##### **Web Functions**
- `tp.web.daily_quote()` - Fetch daily quote
- `tp.web.random_picture()` - Fetch random picture
- `tp.web.request()` - Make HTTP requests

##### **Config Functions**
- Access to configuration values
- Module system for extensibility

#### **Script Engine Features**
- Variable declarations and assignments
- Arithmetic operations (+, -, *, /, %)
- Comparison operators (==, !=, <, >, <=, >=)
- Logical operators (&&, ||, !)
- String operations and methods
- Array operations and methods
- Object property access
- Function declarations and calls
- Control flow (if/else, for, while)
- Try/catch error handling
- Template literals with interpolation

#### **IDE Integration (JetBrains)**
- Template execution action
- Code completion for template functions
- Syntax highlighting for template blocks
- Settings panel for configuration
- Error reporting and validation

#### **Parallel Execution**
- Automatic dependency analysis
- Parallel scheduling for independent blocks
- Progress reporting
- Cancellation support

#### **Module System**
- Config module for configuration access
- Frontmatter module for YAML frontmatter
- Hooks module for lifecycle hooks
- Extensible module factory

#### **Testing**
- Comprehensive test suite with 200+ tests
- Integration tests for full template workflows
- Unit tests for all major components
- Mock services for isolated testing

#### **Documentation**
- Complete API reference
- Architecture documentation
- 10+ example templates demonstrating features
- README with quick start guide

#### **Build System**
- Gradle-based build configuration
- KSP code generation for handlers
- Multi-platform support (IntelliJ, Rider)
- Automated handler registration

