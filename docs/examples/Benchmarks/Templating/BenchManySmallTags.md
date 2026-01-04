<%* const t0 = (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now(); %>

# Many small tags benchmark

Below are many small tags. Duplicate the block to scale it.

<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>

<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>
<% tp.file.title %>

<%* const t1 = (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
tR += `\n\nTime for this template render segment: ${(t1 - t0).toFixed(3)} ms\n`;
%>
