# <img src="src/main/resources/META-INF/pluginIcon.svg" alt="" width="24" height="24"> Pretty JSON Log plugin

![Build](https://github.com/orangain/pretty-json-log-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/io.github.orangain.prettyjsonlog.svg)](https://plugins.jetbrains.com/plugin/24693-pretty-json-log/versions)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/io.github.orangain.prettyjsonlog.svg)](https://plugins.jetbrains.com/plugin/24693-pretty-json-log)

![Plugin screenshot](media/screenshot_expanded.png)

<!-- Plugin description -->
Pretty JSON Log plugin for IntelliJ Platform makes NDJSON (Newline Delimited JSON a.k.a. JSON Lines) logs more readable
in the console. It has the following features:

- **JSON Parsing**: Automatically parses each log line as JSON and extracts essential log information such as timestamp,
  log level, and message.
- **Colorful Display**: Displays essential log information in different colors depending on the log level to make it
  easier to read.
- **Readable Timestamp**: Formats the timestamp in a human-friendly format.
- **Expandable Pretty JSON**: Prints a well-formatted JSON string following the log message. The JSON string is folded
  by default, but you can expand it when you need to view the full details.
- **Seamless Integration**: Supports various log formats such
  as [Logstash](https://github.com/logfellow/logstash-logback-encoder), [Bunyan](https://github.com/trentm/node-bunyan),
  [Pino](https://github.com/pinojs/pino), [log/slog](https://pkg.go.dev/log/slog),
  [Cloud Logging](https://cloud.google.com/logging/docs/structured-logging), etc. with no additional configuration or
  software.

This plugin is useful when you are developing a modern system that outputs logs in JSON format. You no longer need to
switch log formats between production and local development environments.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Pretty JSON
  Log"</kbd> > <kbd>Install</kbd>

  ![Installation dialog](media/screenshot-installation.png)

- Manually:

  Download the [latest release](https://github.com/orangain/pretty-json-log-plugin/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

After installation, newly written JSON lines logs in the console will be automatically formatted by the plugin.

## Recommended Settings

The following settings will prevent strange display of folding markers when a stack trace is included in the
JSON log.

- <kbd>Settings/Preferences</kbd> > <kbd>Editor</kbd> > <kbd>General</kbd> > <kbd>Console</kbd> >
  <kbd>Exceptions</kbd> > <kbd>+</kbd> > <kbd>Enter `":`</kbd> > <kbd>OK</kbd>

  ![Recommended Settings](media/settings-folding-exception.png)

Before:

![Two folding markers and "internal line" text](media/settings-folding-before.png)

After:

![One folding marker](media/settings-folding-after.png)

## Acknowledgements

This plugin is inspired by the [pino-pretty](https://github.com/pinojs/pino-pretty)
and [bunyan CLI](https://github.com/trentm/node-bunyan). The great idea behind these tools is that applications should
write logs in machine-readable format (JSON) and pretty-printing for human readability should be done by another tool. I
am grateful to the authors of these tools.

Thanks to the [IntelliJ Platform Plugin Template][template], I was able to quickly start developing the plugin.

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
