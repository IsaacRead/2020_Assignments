# ENGR 301: Architectural Design and Proof-of-Concept

## Proof-of-Concept

The aim of an architectural proof-of-concept (spike or walking skeleton) is to demonstrate the technical feasibility of your chosen architecture, to mitigate technical and project risks, and to plan and validate your technical and team processes (e.g., build systems, story breakdown, Kanban boards, acceptance testing, deployment).

A walking skeleton is an initial technical attempt that will form the architectural foundation of your product. Since a walking skeleton is expected to be carried into your product, it must be completed to the quality standards expected for your final product. A walking skeleton should demonstrate all the technologies your program will rely on "end-to-end" &mdash; from the user interface down to the hardware.

In the context of ENGR 301, a walking skeleton does not need to deliver any business value to your project: the aim is technical validation and risk mitigation.


## Document

The aim of the architectural design document is to describe the architecture and high-level design of the system your group is to build, to identify any critical technical issues with your design, and to explain how you have addressed the highest rated technical and architectural risks. The architecture document should also demonstrate your understanding of architectural techniques and architectural quality, using tools and associated notations as necessary to communicate the architecture precisely, unambiguously and clearly in a written technical document.

Page specifications below are *limits not targets* and refer to the pages in the PDF generated from the markdown. Because the size of your document is necessarily limited, you should ensure that you focus your efforts on those architectural concerns that are most important to completing a successful system: if sections are at their page limit, indicate how many items would be expected in a complete specification.

The ENGR 301 project architecture design document should be based on the standard ISO/IEC/IEEE 42010:2011(E) _Systems and software engineering &mdash; Architecture description_, plus appropriate sections from ISO/IEC/IEEE 29148:2018(E) _Systems and software engineering &mdash; Life cycle processes &mdash; Requirements engineering_; ISO/IEC/IEEE 15289:2017 _Systems and software engineering &mdash; Content of life-cycle information items (documentation)_; ISO/IEC/IEEE 15288:2015 _Systems and software engineering &mdash; System life-cycle processes_; ISO/IEC/IEEE 12207:2017 _Systems and software engineering &mdash; Software life cycle processes_ and ISO 25010 SQuaRE; with notations from ISO/ISE 19501 (UML). In particular, Annex F of ISO/IEC/IEEE 15288 and Annex F of ISO/IEC/IEEE 12207. These standards are available through the Victoria University Library subscription to the [IEEE Xplore Digital Library](https://ieeexplore.ieee.org/) (e.g., by visiting IEEE Xplore from a computer connected to the University network).

The document should contain the sections listed below, and conform to the formatting rules listed at the end of this brief.

All team members are expected to contribute equally to the document and list their contributions in the last section of the document (please make sure that your continued contribution to this document can be traced in GitLab). You should work on your document in your team's GitLab repository in a directory called "M2_Architecture". If more than one team member has contributed to a particular commit, all those team member IDs should be included in the first line of the git commit message. ``git blame``, ``git diff``, file histories, etc. will be tools used to assess individual contributions, so everyone is encouraged to contribute individually (your contribution should be made to many sections of the document, rather than focusing on just a single section), commit early and commit often.

---

# ENGR 301 Project *NN* Architectural Design and Proof-of-Concept

#### Bryan Lim, Kaustubh Pawar, Isaac Read, Mohammad Shaik, Daniel Vidal Soroa, James West

## 1. Introduction

One page overall introduction including sections 1.1 and 1.2 (ISO/IEC/IEEE 42010:2011(E) clause 5.2)

### Client

The client is Andre Geldenhuis, he can be contacted through Mattermost at @geldena.

### 1.1 Purpose

This software, Rocket Mission Control, will allow users to launch, monitor, and adjust rocket systems before, during, and after flight.

### 1.2 Scope

Rocket Mission Control is a standalone program which will allow users to get information from a rocket, safely launch the rocket, monitor the rocket data during flight, and allow small adjusts during flight. The system will integrate with rocket hardware and retrieve weather data to assist with the flight. The goal of the project is to build a safe mission control software that can improve the launch and flight of rockets.

### 1.3 Changes to requirements

The requirement have not changed significantly since the requirements document.

## 2. References

References to other documents or standards. Follow the IEEE Citation Reference scheme, available from the [IEEE website](https://ieee-dataport.org/sites/default/files/analysis/27/IEEE%20Citation%20Guidelines.pdf) (PDF; 20 KB). (1 page, longer if required)

## 3. Architecture

Describe your system's architecture according to ISO/IEC/IEEE 42010:2011(E), ISO/IEC/IEEE 12207, ISO/IEC/IEEE 15289 and ISO/IEC/IEEE 15288.

Note in particular the note to clause 5 of 42010:

_"The verb include when used in Clause 5 indicates that either the information is present in the architecture description or reference to that information is provided therein."_

This means that you should refer to information (e.g. risks, requirements, models) in this or other documents rather than repeat information.

### 3.1 Stakeholders

See ISO/IEC/IEEE 42010 clause 5.3 and ISO/IEC/IEEE 12207 clause 6.4.4.3(2).

For most systems this will be about 2 pages, including a table mapping concerns to stakeholder.

### 3.2 Architectural Viewpoints
(1 page, 42010 5.4) 

Identify the architectural viewpoints you will use to present your system's architecture. Write one sentence to outline each viewpoint. Show which viewpoint frames which architectural concern.

### 4. Architectural Views

(5 sub-sections of 2 pages each sub-section, per 42010, 5.5, 5.6, with reference to Annex F of both 12207 and 15288) 

Describe your system's architecture in a series of architectural views, each view corresponding to one viewpoint.

You should include views from the following viewpoints (from Kruchten's 4+1 model):

 * Logical
 * Development
 * Process
 * Physical 
 * Scenarios - present scenarios illustrating how two of your most important use cases are supported by your architecture

As appropriate you should include the following viewpoints:

 * Circuit Architecture
 * Hardware Architecture

Each architectural view should include at least one architectural model. If architectural models are shared across views, refer back to the first occurrence of that model in your document, rather than including a separate section for the architectural models.

### 4.1 Logical
...

### 4.2 Development
...

### 4.3 Process
...

### 4.4 Physical 
...

### 4.5 Scenarios
...

## 5. Development Schedule

The conditions accorded to release a minimum viable product are to satisfy the most relevant user stories/features. The most significant user stories by its priority order are:
1.	Read serial Data in CSV format and populate line chart and text boxes (Activity).
* [ ] Connect to ground station (Task)
* [ ] Read and manipulate serial Data (Task).
* [ ] Populate Line chart (Task).
* [ ] Populate text box (Task).
* [ ] Update rocket state (Task).


2.	Read weather data from the web site and populate the text boxes (Activity).
* [ ] Connect to Open Weather web site (Task).
* [ ] Request weather data according GPS location (Task).
* [ ] Populate text boxes (Task).

3.	Request to MCS minimum landing area (Activity).
* [ ] Connect to MCS (Task).
* [ ] Request landing area to MCS (Task).
* [ ] Populate text boxes (Task).

4.	 Send go signal (Activity).
* [ ]  Send go signal to ground station (Task).

### 5.1 Schedule

According these user stories, the development schedule will be structured as follow:
* Week 8 preparing for sprint planning meeting
* Week 9 (Sprint 1)
  * (25/05/20) sprint 1 planning meeting:
  *	Goal: Satisfy user story 1.
  *	Time: Weeks 9-10
* Week 11 (Sprint 2)
  * (08/06/20): Test acceptance criteria for user story 1.
  * (09/06/20): Sprint 2 planning meeting:
  * Goal: Satisfy user story 2.
  * Time: Weeks 11-12.
* Week 1 (Sprint 3)
  * (First meeting): Test acceptance criteria for user story 2.
  * (Next meeting): Sprint 3 planning meeting:
  * Goal: Satisfy user story 3.
  * Time: Weeks 1-2.
* Week 3 (Sprint 4) 
  * (First meeting): Test acceptance criteria for user story 3.
  * (Next meeting): Sprint 4 planning meeting:
  * Goal: Satisfy user story 4.
  * Time: Weeks 3-4.
* Week 5: Test acceptance criteria for user story 4 and minimum viable product.
* Week 6-7 – Sprint 5.
* Week 8-9 – Sprint 6…

1. architectural prototype

2. Minimum viable product will be release during week 5 of the second trimester when the product satisfies the four most relevant features (prioritized user stories). 

3. Further releases and extra feature will be accorded and assigned after week 5 and developed during the next sprints.

### 5.2 Budget and Procurement

#### 5.2.1 Budget

Present a budget for the project (as a table), showing the amount of expenditure the project requires and the date(s) on which it will be incurred. Substantiate each budget item by reference to fulfilment of project goals (one paragraph per item).

(1 page). 

#### 5.2.2 Procurement

Present a table of goods or services that will be required to deliver project goals and specify how they are to be procured (e.g. from the School or from an external organisation). These may be software applications, libraries, training or other infrastructure, including open source software. Justify and substantiate procurement with reference to fulfilment of project goals, one paragraph per item.
(1 page).

### 5.3 Risks 

1. Risk: Since, this software is been created individually as a team, it might create problems for developer in estimating scheduling development time.<br>
   Risk type: Schedule risk.<br>
   Likelihood: 3/5 <br>
   Impact: 5/5 <br>
   Mitigation: All members will be attending weekly group meetings to discuss and at the same monitor on each others works.<br>

2. Risk: Improper process implementation, ambiguity in individual responsibilities and unstructured prorities could be a potential risk for project.<br>
   Risk type: Strategic risk.<br>
   Likelihood: 3/5 <br>
   Impact: 5/5 <br>
   Mitigation: With the help of gitlab all members could create issues and assign themselves to it. This will help them to track and prioritize individual responsibilities.<br>

3. Risk: Unforeseen circumstances like technology obsolesence, changes in government policies, pandemics may intensify the risk factor involved in project development. <br>
   Risk type: External Risk.<br>
   Likelihood: 3/5 <br>
   Impact: 5/5 <br>
   Mitigation: By virtue of assumption analysis and proper strategic planning a team can minimize the risk from such unforeseen circumstances.<br>

4. Risk: Since, software developers rely on their hands to practice their craft, injuries such as carpal tunnel, shoulder strain etc. are somethings to be considered as it may lead to a potential health and safety risk. <br>
   Risk type: Health and safety.<br>
   Likelihood: 2/5 <br>
   Impact: 5/5 <br>
   Mitigation: A proper work plan and a little bit of excercising may help mitigate this risk.<br>

SCHEDULING RISK
Description: considering the abstract nature and uniqueness of the program, Software development is inherently difficult to estimate and plan, Projections of the tasks are either optimistic or based on a template. Project managers may find it challenging to estimate the time, scope and resources required to complete the project. This will lead to unrealistic schedule, budget, ambiguous scope and inadequate resources, which are known to be the major causes of project failure. 
Type: Planning; Likelihood: 0.65; Impact: 10
Mitigation strategies: Get the team involved more in the preparation and estimation. Get early reviews and escalate the issues directly with stakeholders. The team 's true momentum easily emerges by operating in small increments. Timely review and schedule adjustments will help to mitigate this risk.

REQUIREMENTS RISK
Description: Issues may arise when system requirements are not been identified appropriately. More and more features which were not identified in the initial stages will come to light once the projects set ahead. These requirements may pose a threat to estimates and schedules.   Unclear, imprecise and nonsensical specifications can result in inconsistency.
Type:  Requirements Management; Likelihood: 0.75; Impact: 10
Mitigation strategies: In the initial phases of application description, team discussions should be held with the end user. Maintaining a close connection with the user community throughout the life cycle of system engineering will help mitigate this risk. Routine trade-off discussions on functionality and requirements should take place at every boundary of iteration.

SPECIFICATIONS RISK
Description: It becomes obvious as coding and integration starts that the specification is incomplete, or includes not understandable requirements. Lack of understanding and coordination between different project groups may result in a breakdown in overall project integration.
Type:  Planning & Execution; Likelihood: 0.50; Impact: 8
Mitigation strategies: Dedicated members should be allocated to have frequent discussions with other teams from the beginning to understand each other's requirements. In our case, contact should be made with the Monte carlo simulation team and the avionics team so that specifications can be communicated and implemented without fail. Making assumptions and working should be minimized.

PRODUCTIVITY RISK
Description: The sense of urgency to work in earnest is often absent due to long project timelines resulting in time lost in initial stages of a project which can never be recovered. It is possible to underestimate the time between initiation and actual deployment of code in the main code branch which can establish high likelihood of delayed delivery.
Type:  Planning & Execution; Likelihood: 0.50; Impact: 9
Mitigation strategies: Time can be bounded by having short iterations which give a sense of urgency. In addition, the percentage of project risk can be substantially reduced by practicing effective communication with the teams, creating a solid organizational structure and implementing elaborate planning.

COMMUNICATION AND INFORMATION FLOW RISK
Description:  Regarding team communication, we often see varying views among members of the team on the open issues and what the goals are, indicating a lack of clear and timely communication, and sometimes some conflict among members. More often however, it is insufficient or ignored communication with clients and management. Usually the issue is with infrequent updates or lack of full disclosure of the released information.
Type:  organizational Management; Likelihood: 0.90; Impact: 7
Mitigation strategies: Team building activities should be carried out frequently to enhance the cooperation between members. Sessions on knowledge sharing and peer reviews should be done as a regular activity.

APPLICATION AND SYSTEM ARCHITECTURE RISK
Description: Taking the wrong path can have catastrophic consequences for a platform or architecture. When a wrong choice has been made then the implementation of the program will not be successfully completed and integration issues can occur later. Furthermore, architectural design method choice can affect programming language choice. If this has not been considered, then developers may choose a language that does not support the method of architectural design used.
Type:  Technical; Likelihood: 0.60; Impact: 9
Mitigation strategies: It is important, as with the technical risks, that the team should include experts who understand the architecture and are able to make sound design choices. All the individual participants must make an effort to learn the technical aspects expected of the project. Simulation and modelling in bits may help avoid failure.

REQUIREMENT DOCUMENT RISK
Description: If developers were not involved in the analysis of requirements and the process of interpretation, then they could not understand the requirements document. Therefore, they would not be able to start their design on a solid knowledge of the system requirements, and therefore can create a design for a system other than the intended one.
Type:  Documentation; Likelihood: 0.60; Impact: 6
Mitigation strategies: All the team members including the developers should be involved during documentation stage. It should not be done by particular individuals.


### 5.4 Health and Safety

Document here project requirements for Health and Safety.

#### 5.4.1 Safety Plans

Safety Plans may be required for some projects, depending on project requirements.


## 6. Appendices

### 6.1 Assumptions and dependencies 

One page on assumptions and dependencies (9.5.7) 

### 6.2 Acronyms and abbreviations

One page glossary as required 

## 7. Contributions

An one page statement of contributions, including a list of each member of the group and what they contributed to this document.

---

## Formatting Rules 

 * Write your document using [Markdown](https://gitlab.ecs.vuw.ac.nz/help/user/markdown#gitlab-flavored-markdown-gfm) in your team's GitLab repository.
 * Major sections should be separated by a horizontal rule.


## Assessment 

This document will be weighted at 20% on the architectural proof-of-concept(s), and 80% on the architecture design.

The proof-of-concept will be assessed for coverage (does it demonstrate all the technologies needed to build your project?) and quality (with an emphasis on simplicity, modularity, and modifiability).

The document will be assessed by considering both presentation and content. Group and individual group members will be assessed by identical criteria, the group mark for the finished PDF and the individual mark on the contributions visible through `git blame`, `git diff`, file histories, etc. 

The presentation will be based on how easy it is to read, correct spelling, grammar, punctuation, clear diagrams, and so on.

The content will be assessed according to its clarity, consistency, relevance, critical engagement and a demonstrated understanding of the material in the course. We look for evidence these traits are represented and assess the level of performance against these traits. Inspection of the GitLab Group is the essential form of assessing this document. While being comprehensive and easy to understand, this document must be reasonably concise too. You will be affected negatively by writing a report with too many pages (far more than what has been suggested for each section above).

---
