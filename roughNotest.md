```markdown
### Application Requirements

1. **Alert Types and Business Units:**
   - Various types of alerts (e.g., transaction type, customer type, AML type).
   - Different business units with specific workflows, including the possibility of starting directly at Level 2.
   - Each alert contains a business unit that determines the review process (one-touch or two-touch).

2. **Workflow Levels:**
   - **Level 1 (Reviewers):**
     - Two operators review alerts, add dispositioning results (positive or false match), and evidence.
     - Alerts can be escalated based on business conditions (e.g., one operator's positive review or both operators' positive reviews).
     - Alerts with false matches are closed.
   - **Level 2 (Investigators):**
     - Similar to Level 1, with two operators reviewing alerts, adding dispositioning results, and evidence.
     - Alerts can be escalated or closed based on dispositioning results and business conditions.
   - **Level 3 (Compliance):**
     - One operator reviews alerts, adds results, and evidence.
     - Alerts are either closed or remain escalated based on results.
     
3. **Review Mechanism:**
   - **One Touch:** One operator within a level adds their review results.
   - **Two Touch:** Two operators review before an alert can be closed or escalated.

4. **Inquiry Mode:**
   - Operators at any level can send alerts into inquiry mode for further information gathering, which may take a day or more.

5. **Escalation Conditions:**
   - Escalation is enabled only if dispositioning results are positive across the reviews. (At least for now)

6. **Closure Conditions:**
   - Alerts with false matches are closed at any level.

7. **Entitlement Rules:** (At least to start with)
   - **Supervisor Queue:**
     - If Level 1 Operator 1 and Level 1 Operator 2 both give a false review, the alert is closed.
     - If one gives a false review and the other gives a true review, the alert goes to a supervisor queue for further review.
   - **Access Levels:**
     - Level 1 Operator 1, Level 1 Operator 2, and Level 1 Operator 3 (supervisor) can have the same roles.
     - The same individual cannot review the results of both Level 1 Operator 1 and Level 1 Operator 2. The review must be done by a different actor or individual.
   - **RBAC Implementation:**
     - Operators' access is based on roles and permissions determined by business units and alert types.
     - Entitlements ensure that operators can only work on alerts associated with their specific business unit and alert type.
     - This RBAC approach applies to all levels, not just Level 1.

8. **UI Queues (As an example of the search filters):**
   - Multiple queues exist within the UI, similar to facets within a search UI.
   - Different queues are associated with different levels of operators, such as Level 1 operators working on Level 1 alerts and Level 2 investigators working on Level 2 alerts.

### Summary of Key Points
- The application must support various alert types and workflows tailored to specific business units.
- Workflows are divided into three levels, each with specific roles and review processes.
- Entitlements and access control are critical, with a role-based access control (RBAC) approach ensuring operators work within their assigned business units and alert types.
- Multiple queues in the UI handle alerts at different levels and stages.

### Current Implementation Overview

1. **Customer Alerts (Actimize):**
   - Using Actimize with a custom plugin for managing alerts.
   - The UI and entitlements are handled through Actimize's case manager.
   - Entitlements and actions are managed via database configurations.
   - Execution and updates are performed based on database configurations and updates.

2. **Transaction Alerts (Homegrown Solution):**
   - Completely custom-built using Oracle DB and stored procedures.
   - Alerts are stored in database tables, tightly coupled with the transaction alert schema.
   - Workflow transitions and states are managed via database tables.
   - Tables contain configurations and transitions for specific business units with default workflows.
   - Decision-making and actions are stored in the database, implemented as stored procedures.
   - Built as a state machine with transitions and execution logic defined in the database.
   - Execution is synchronous, matching the user experience expectations.

### Key Points:
- **Customer Alerts:** Managed with Actimize and a custom plugin, with entitlements and workflows driven by database configurations.
- **Transaction Alerts:** Custom database solution using Oracle DB with tightly coupled schema, state management, transitions, and execution logic implemented in stored procedures.
- **General Requirements:** 
  - Looking for a homegrown solution not dependent on the database for workflow logic.
  - Database usage should be limited to storing outputs and supporting UI updates and search filters.

#### Challenges and Requirements
1. **Current Challenges:**
   - Integrate disparate systems for a unified 360-degree view of customer and transaction-specific alerts.
   - Support multiple different alert types within a single system.

2. **Performance and Scalability:**
   - No significant issues with the current synchronous execution model.
   - Maintain a synchronous execution experience in the new solution.

3. **Integration Needs:**
   - Support external integrations without extensive coding for new alert types.
   - Customization needed for changes in workflow levels, but minimal coding required.

4. **User Experience:**
   - Maintain current user experience with enhanced features like 360-degree support.
   - Alerts should be locked to operators during review, with the intent to use Document DB.
   - Current locking achieved using RDBMS update skip lock.

5. **Technology Preferences:**
   - Avoid database dependency for workflow logic.
   - Preference for using Document DB.
   - Use open-source BPMN engines like Camunda or Kogito.
   - Use Spring State Machine for state management.

6. **Deployment and Maintenance:**
   - Cloud-agnostic and Kubernetes-ready deployment.

#### Functional Requirements
1. **Alert Types and Business Units:**
   - Various types of alerts (e.g., transaction type, customer type, AML type).
   - Different business units with specific workflows, including starting directly at Level 2.

2. **Workflow Levels:**
   - **Level 1 (Reviewers):** Two operators review alerts, add dispositioning results and evidence, with possible escalation based on business conditions.
   - **Level 2 (Investigators):** Similar to Level 1, with two operators reviewing alerts, adding dispositioning results and evidence.
   - **Level 3 (Compliance):** One operator reviews alerts, adds results and evidence.

3. **Review Mechanism:**
   - **One Touch:** One operator within a level adds their review results.
   - **Two Touch:** Two operators review before an alert can be closed or escalated.

4. **Inquiry Mode:**
   - Operators at any level can send alerts into inquiry mode for further information gathering.

5. **Escalation Conditions:**
   - Enabled only if dispositioning results are positive across the reviews.

6. **Closure Conditions:**
   - Alerts with false matches are closed at any level.

7. **Entitlement Rules:**
   - **Supervisor Queue:** Specific conditions for escalations to a supervisor queue.
   - **Access Levels:** Roles and permissions based on business units and alert types.
   - **RBAC Implementation:** Operator's access controlled by roles and permissions, ensuring they work within their assigned business units and alert types.

8. **UI Queues:**
   - Multiple queues within the UI for different levels of operators, similar to facets within a search UI.
