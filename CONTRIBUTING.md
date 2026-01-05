# Contributing to OAS SDK

Thank you for your interest in contributing to OAS SDK! This document provides guidelines and instructions for contributing.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs. actual behavior
- Environment details (Java version, OS, etc.)
- Relevant code snippets or error messages

### Suggesting Enhancements

We welcome feature suggestions! Please create an issue with:
- A clear description of the enhancement
- Use cases and examples
- Potential implementation approach (if you have ideas)

### Pull Requests

1. **Fork the repository** and create a feature branch
2. **Follow coding standards**:
   - Use Java 21 features appropriately
   - Follow existing code style and formatting
   - Add JavaDoc comments for public APIs
   - Write unit tests for new features
   - Ensure all tests pass (`mvn clean test`)
3. **Update documentation**:
   - Update README.md if adding new features
   - Add examples if applicable
   - Update CHANGELOG.md
4. **Submit the pull request**:
   - Provide a clear description
   - Reference related issues
   - Ensure CI checks pass

## Development Setup

1. **Prerequisites**:
   - Java 21 or higher
   - Maven 3.6 or higher
   - Git

2. **Clone and build**:
   ```bash
   git clone https://github.com/egain/oas-sdk-java.git
   cd oas-sdk-java
   mvn clean install
   ```

3. **Run tests**:
   ```bash
   mvn test
   ```

## Coding Standards

- **Java Style**: Follow Oracle Java Code Conventions
- **Naming**: Use descriptive names, follow camelCase for variables/methods, PascalCase for classes
- **Documentation**: All public classes and methods must have JavaDoc comments
- **Testing**: Aim for high test coverage, especially for new features
- **Error Handling**: Use appropriate exception types, provide meaningful error messages

## Testing Guidelines

- Write unit tests for all new features
- Ensure existing tests continue to pass
- Add integration tests for complex features
- Test edge cases and error conditions

## Commit Messages

Use clear, descriptive commit messages:
- Start with a verb (Add, Fix, Update, Remove, etc.)
- Be specific about what changed
- Reference issue numbers when applicable

Example:
```
Add support for OpenAPI 3.1.0 specification parsing
Fix null pointer exception in reference resolution
Update README with new feature documentation
```

## Review Process

- All pull requests require review
- Address review comments promptly
- Maintain a constructive and respectful tone
- Be open to feedback and suggestions

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

If you have questions, please:
- Check existing issues and discussions
- Create a new issue with the `question` label
- Review the README.md for documentation

Thank you for contributing to OAS SDK!

