# Minions Framework

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Minions is a powerful framework for building, managing, and deploying AI agents. It provides a robust foundation for creating intelligent systems that can understand, process, and respond to complex tasks while maintaining high standards of reliability and scalability.

## 🌟 Features

- **Component-Based Prompt Management**: Organize and version your AI prompts with ease
- **Multi-Tenant Support**: Deploy securely across different organizations
- **Flexible Agent Types**: Support for various AI agent configurations
- **Built-in Versioning**: Track and manage prompt versions effortlessly
- **Enterprise Ready**: Designed for production use with security in mind

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- MongoDB 5.0+

### Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.minionslab</groupId>
    <artifactId>minions-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
// Create a new prompt
MinionPrompt prompt = MinionPrompt.builder()
    .name("CustomerServicePrompt")
    .type(MinionType.USER_DEFINED_AGENT)
    .version("1.0.0")
    .build();

// Add components to your prompt
prompt.addComponent(PromptComponent.builder()
    .type(PromptType.INSTRUCTION)
    .text("Handle customer inquiries professionally")
    .build());
```

## 📚 Documentation

For detailed documentation, please visit our [documentation site](https://docs.minionslab.com).

## 🛠️ Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/minionslab/minions.git

# Navigate to the project directory
cd minions

# Build the project
mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MinionPromptTest
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Thanks to all our contributors
- Inspired by modern AI development practices
- Built with Spring Boot and MongoDB

## 📞 Support

- [GitHub Issues](https://github.com/minionslab/minions/issues)
- [Documentation](https://docs.minionslab.com)
- [Community Forum](https://community.minionslab.com)

## 🔄 Project Status

See our [Vision Document](VISION.md) for detailed roadmap and milestones.

## 🌐 Community

- [Discord](https://discord.gg/minionslab)
- [Twitter](https://twitter.com/minionslab)
- [Blog](https://blog.minionslab.com)

---

Made with ❤️ by the Minions Team 