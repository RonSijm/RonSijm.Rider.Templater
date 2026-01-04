package ronsijm.templater.standalone.ui

import ronsijm.templater.standalone.ui.util.ScrollPaneWrapper
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.text.html.HTMLEditorKit

class RenderPanel : JPanel(BorderLayout()) {

    private val editorPane = JEditorPane()
    private val scrollPane = JScrollPane(editorPane)

    init {
        setupUI()
    }

    private fun setupUI() {
        editorPane.contentType = "text/html"
        editorPane.editorKit = HTMLEditorKit()
        editorPane.isEditable = false

        add(ScrollPaneWrapper.wrap(scrollPane), BorderLayout.CENTER)

        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { }
        toolbar.add(refreshButton)

        add(toolbar, BorderLayout.NORTH)
    }

    fun setRenderedContent(html: String) {
        editorPane.text = html
        editorPane.caretPosition = 0
        editorPane.revalidate()
        editorPane.repaint()
    }

    fun setMarkdownContent(markdown: String) {
        val html = convertMarkdownToHtml(markdown)
        setRenderedContent(html)
    }

    fun clear() {
        editorPane.text = ""
    }

    private fun convertMarkdownToHtml(markdown: String): String {
        val sb = StringBuilder()
        sb.append("<html><head><style>")
        sb.append("body { font-family: sans-serif; padding: 10px; }")
        sb.append("h1 { color: #333; }")
        sb.append("h2 { color: #555; }")
        sb.append("h3 { color: #777; }")
        sb.append("code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }")
        sb.append("pre { background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow-x: auto; }")
        sb.append("</style></head><body>")

        val lines = markdown.lines()
        var inCodeBlock = false
        var inList = false

        for (line in lines) {
            when {
                line.startsWith("```") -> {
                    if (inCodeBlock) {
                        sb.append("</pre>")
                        inCodeBlock = false
                    } else {
                        sb.append("<pre><code>")
                        inCodeBlock = true
                    }
                }
                inCodeBlock -> {
                    sb.append(escapeHtml(line)).append("\n")
                }
                line.startsWith("# ") -> {
                    sb.append("<h1>").append(escapeHtml(line.substring(2))).append("</h1>")
                }
                line.startsWith("## ") -> {
                    sb.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>")
                }
                line.startsWith("### ") -> {
                    sb.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>")
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    if (!inList) {
                        sb.append("<ul>")
                        inList = true
                    }
                    sb.append("<li>").append(escapeHtml(line.substring(2))).append("</li>")
                }
                line.isBlank() -> {
                    if (inList) {
                        sb.append("</ul>")
                        inList = false
                    }
                    sb.append("<br/>")
                }
                else -> {
                    if (inList) {
                        sb.append("</ul>")
                        inList = false
                    }
                    sb.append("<p>").append(processInlineMarkdown(escapeHtml(line))).append("</p>")
                }
            }
        }

        if (inCodeBlock) {
            sb.append("</code></pre>")
        }
        if (inList) {
            sb.append("</ul>")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun processInlineMarkdown(text: String): String {
        var result = text

        result = result.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")

        result = result.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")

        result = result.replace(Regex("`(.+?)`"), "<code>$1</code>")
        return result
    }
}

