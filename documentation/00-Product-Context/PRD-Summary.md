# MimoSe - "Leading Self" Application

## Product Overview

**MimoSe** is a personal growth application designed to help users understand and manage themselves through self-reflection and social awareness. The app follows the philosophy of "Leading Self" – the belief that before leading others, one must first understand and lead oneself.

## Core Concepts

### 1. Innerverse (Inner World)
The internal landscape of the user's emotional and energetic states.

- **Energy Tracking**: Monitor personal energy levels throughout the day
- **Context Awareness**: Tag energy readings with situational context (work, social, rest, etc.)
- **Core Values**: Define and maintain personal core values that guide decision-making

### 2. Outerverse (Outer World)
The external social environment and relationships that affect the user.

- **Social Connections**: Map relationships with friends, family, and colleagues
- **Orbit System**: Organize people into orbits based on relationship closeness
- **Impact Assessment**: Measure how different relationships affect personal energy

### 3. Bridge (Connection)
The integration point between inner self and outer world.

- **Action Protocols**: Pre-defined responses to triggers and situations
- **Script Templates**: Guided scripts for handling challenging interactions
- **Pattern Recognition**: Identify recurring patterns between energy and social interactions

## Backend Data Requirements

### Data Tracking Needs
| Domain | Data Points |
|--------|-------------|
| **Energy** | Level (1-10), timestamp, context tags, notes |
| **Relationships** | Friend name, orbit distance (1-5), impact score, interaction history |
| **Protocols** | Trigger conditions, response scripts, usage frequency |

### Key Calculations
- **Orbit Distance**: Calculated based on interaction frequency, energy impact, and explicit user ranking
- **Energy Patterns**: Time-series analysis of energy levels correlated with context tags
- **Impact Score**: Weighted average of how a relationship affects user's energy over time

## Integration Points

This Backend serves the MimoSe Frontend application, providing:
- RESTful APIs for all data operations
- Real-time data synchronization
- Secure user authentication
- Data persistence with PostgreSQL
