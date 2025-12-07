# NGINX Log Analyzer

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=for-the-badge&logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Coverage](https://img.shields.io/badge/Coverage-50%25+-yellow?style=for-the-badge)](https://jacoco.org/)

A powerful command-line tool for analyzing NGINX access logs with support for multiple input sources and output formats.

## Features

- **Multiple Input Sources** - Local files, glob patterns, and remote URLs
- **Comprehensive Statistics** - Request counts, response sizes, status codes, and more
- **Percentile Calculations** - 95th percentile response size analysis
- **Date Filtering** - Filter logs by date range (ISO8601 format)
- **Multiple Output Formats** - JSON, Markdown, and AsciiDoc
- **Memory Efficient** - Stream-based processing for large files
- **Smart Parsing** - Handles malformed log entries gracefully

## Installation

### Prerequisites

- Java 21 or higher
- Maven 3.9+

### Build from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/nginx-log-analyzer.git
cd nginx-log-analyzer

# Build the project
mvn clean package shade:shade -DskipTests

# The JAR file will be located at
ls target/hw3-logs-1.0.jar
```

## Usage

### Basic Syntax

```bash
java -jar nginx-log-analyzer.jar --path <source> --format <format> --output <file> [options]
```

### Command Line Options

| Option | Short | Required | Description |
|--------|-------|----------|-------------|
| `--path` | `-p` | ✅ | Path to log file(s). Supports local files, glob patterns, and URLs |
| `--format` | `-f` | ✅ | Output format: `json`, `markdown`, or `adoc` |
| `--output` | `-o` | ✅ | Output file path |
| `--from` | | ❌ | Start date filter (ISO8601 format) |
| `--to` | | ❌ | End date filter (ISO8601 format) |

### Exit Codes

| Code | Description |
|------|-------------|
| `0` | Success |
| `1` | Unexpected error |
| `2` | Invalid usage (wrong parameters, missing files, etc.) |

## Output Formats

### JSON

Structured output following a strict JSON schema, perfect for further processing.

```json
{
  "files": ["access.log"],
  "totalRequestsCount": 51462,
  "responseSizeInBytes": {
    "average": 659.22,
    "max": 1768.00,
    "p95": 1100.50
  },
  "resources": [
    {"resource": "/downloads/product_1", "totalRequestsCount": 30285}
  ],
  "responseCodes": [
    {"code": 200, "totalResponsesCount": 10234}
  ]
}
```

### Markdown

Human-readable tables, ideal for documentation and reports.

```markdown
#### General Information

| Metric                        | Value      |
|:------------------------------|:-----------|
| Files                         | access.log |
| Total Requests                | 51462      |
| Average Response Size         | 659.22 B   |
```

### AsciiDoc

Professional documentation format with rich formatting support.

```asciidoc
---- General Information ----
Files: test.txt
Total Requests: 11
Average Response Size: 451.36b
Max Response Size: 3316.0b
95th Percentile Size: 1903.0b

---- Requested Resources ----
- /downloads/product_1: 8 requests
- /downloads/product_2: 3 requests

---- Status Codes ----
- 304: 6 responses
- 200: 3 responses
- 404: 2 responses
```

## Statistics Collected

| Statistic | Description |
|-----------|-------------|
| **Total Requests** | Total number of processed log entries |
| **Average Response Size** | Mean response body size in bytes |
| **Max Response Size** | Maximum response body size in bytes |
| **95th Percentile** | 95% of responses are smaller than this value |
| **Top Resources** | Top 10 most frequently requested resources |
| **Response Codes** | Distribution of HTTP status codes |
| **Requests per Date** | Daily request distribution with percentages |
| **Unique Protocols** | List of unique protocols (HTTP/1.1, HTTP/2, etc.) |

## Examples

### Analyze Local File

```bash
java -jar nginx-log-analyzer.jar \
  --path /var/log/nginx/access.log \
  --format json \
  --output report.json
```

### Analyze Multiple Files with Glob Pattern

```bash
java -jar nginx-log-analyzer.jar \
  --path "logs/2024*.log" \
  --format markdown \
  --output report.md
```

### Analyze Remote Log File

```bash
java -jar nginx-log-analyzer.jar \
  --path https://example.com/logs/access.log \
  --format adoc \
  --output report.ad
```

### Filter by Date Range

```bash
java -jar nginx-log-analyzer.jar \
  --path access.log \
  --format json \
  --output report.json \
  --from 2024-01-01 \
  --to 2024-12-31
```

### Analyze from Start Date

```bash
java -jar nginx-log-analyzer.jar \
  --path access.log \
  --format markdown \
  --output report.md \
  --from 2024-06-01
```

## Testing

### Run All Tests

```bash
mvn clean verify
```

### Run Unit Tests Only

```bash
mvn test
```

### Run with Coverage Report

```bash
mvn clean verify jacoco:report
# Open target/site/jacoco/index.html
```

### Test Coverage

The project maintains **50%+ code coverage** with comprehensive tests for:

- Log parsing
- Statistics calculation
- Export formats
- Input validation
- Error handling

## Supported Log Format

The analyzer supports standard NGINX combined log format:

```
$remote_addr - $remote_user [$time_local] "$request" $status $body_bytes_sent "$http_referer" "$http_user_agent"
```

### Example Log Entry

```
93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3"
```

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 21** | Core language with modern features |
| **Maven** | Build and dependency management |
| **Picocli** | Command-line argument parsing |
| **Jackson** | JSON serialization |
| **CommonMark** | Markdown generation |
| **Log4j2** | Logging framework |
| **JUnit 5** | Unit testing |
| **AssertJ** | Fluent assertions |

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
