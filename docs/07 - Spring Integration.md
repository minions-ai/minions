Minions is built around idiomatic Spring patterns:

- Use `@Component` to register custom steps, processors, tools, strategies
- `@ConfigurationProperties` for loading recipe or agent configuration from YAML or application context
- Conditional wiring using Spring Boot's auto-config features

You can fully extend Minions without modifying its internals, using standard Spring idioms.