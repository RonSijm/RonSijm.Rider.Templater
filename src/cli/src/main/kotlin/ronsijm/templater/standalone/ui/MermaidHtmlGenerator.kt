package ronsijm.templater.standalone.ui

object MermaidHtmlGenerator {



    @Suppress("LongMethod")
    fun generateHtml(mermaidCode: String, title: String = "Mermaid Diagram"): String {
        val escapedCode = mermaidCode
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("$", "\\$")

        return buildHtmlPage(title, mermaidCode, escapedCode)
    }

    private fun buildHtmlPage(title: String, mermaidCode: String, escapedCode: String): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <script src="https:
    <style>
        ${getCssStyles()}
    </style>
</head>
<body>
    <div class="header">
        <h1>$title</h1>
        <div class="controls">
            <button class="btn-secondary" onclick="toggleCode()">Toggle Code</button>
            <button class="btn-primary" onclick="downloadSvg()">Download SVG</button>
        </div>
    </div>
    <div class="container">
        <div class="diagram-container">
            <pre class="mermaid">
$mermaidCode
            </pre>
        </div>
        <div class="code-toggle">
            <pre class="code-block" id="codeBlock">$escapedCode</pre>
        </div>
    </div>
    <script>
        ${getJavaScript()}
    </script>
</body>
</html>
        """.trimIndent()
    }

    private fun getCssStyles(): String = """
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #1e1e1e; color: #d4d4d4;
            min-height: 100vh; display: flex; flex-direction: column;
        }
        .header {
            background-color: #2d2d2d; padding: 15px 20px;
            border-bottom: 1px solid #3c3c3c;
            display: flex; justify-content: space-between; align-items: center;
        }
        .header h1 { font-size: 18px; font-weight: 500; color: #e0e0e0; }
        .controls { display: flex; gap: 10px; }
        .controls button {
            padding: 8px 16px; border: none; border-radius: 4px;
            cursor: pointer; font-size: 13px; transition: background-color 0.2s;
        }
        .btn-primary { background-color: #0e639c; color: white; }
        .btn-primary:hover { background-color: #1177bb; }
        .btn-secondary { background-color: #3c3c3c; color: #d4d4d4; }
        .btn-secondary:hover { background-color: #4c4c4c; }
        .container { flex: 1; display: flex; flex-direction: column; padding: 20px; overflow: auto; }
        .diagram-container {
            flex: 1; display: flex; justify-content: center; align-items: flex-start;
            padding: 20px; background-color: #252526; border-radius: 8px; overflow: auto;
        }
        .mermaid { background-color: white; padding: 20px; border-radius: 8px; max-width: 100%; }
        .error { color: #f48771; padding: 20px; text-align: center; }
        .code-toggle { margin-top: 15px; }
        .code-block {
            background-color: #1e1e1e; border: 1px solid #3c3c3c; border-radius: 4px;
            padding: 15px; font-family: 'Consolas', 'Courier New', monospace;
            font-size: 13px; overflow-x: auto; white-space: pre;
            display: none; margin-top: 10px;
        }
        .code-block.visible { display: block; }
    """.trimIndent()

    private fun getJavaScript(): String = """
        mermaid.initialize({
            startOnLoad: true, theme: 'default', securityLevel: 'loose',
            flowchart: { useMaxWidth: true, htmlLabels: true, curve: 'basis' }
        });
        function toggleCode() {
            const codeBlock = document.getElementById('codeBlock');
            codeBlock.classList.toggle('visible');
        }
        function downloadSvg() {
            const svg = document.querySelector('.mermaid svg');
            if (svg) {
                const svgData = new XMLSerializer().serializeToString(svg);
                const blob = new Blob([svgData], { type: 'image/svg+xml' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url; a.download = 'diagram.svg';
                document.body.appendChild(a); a.click();
                document.body.removeChild(a); URL.revokeObjectURL(url);
            } else { alert('No diagram to download'); }
        }
    """.trimIndent()
}

