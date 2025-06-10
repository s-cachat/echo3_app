# What is Echo?
Echo is an open source framework for developing rich web applications. From the developer's perspective, Echo behaves as a user interface toolkit like Java's Swing or Eclipse SWT and completely abstracts developers from the web tier. Ajax technology is employed to deliver a user experience to web clients that approaches that of traditional desktop applications. Echo applications can be created entirely in server-side Java code using a component-oriented and event-driven API or as client-side applications written entirely in JavaScript.

# Why Echo?
Although Model-View-Controller (MVC) design principles and the Java Swing API are well established and well known, most web application frameworks on the market are still stuck in the paradigm of page-flow oriented design principles and require a heavy mixture of different technologies like XML, Java, JSP, tag libraries, etc. Such a paradigm has significant impacts on the development process as it disables full reuse and requires developers to generate, write, and maintain many different artifacts during the whole process: Think of JSP template files, XML-based page flow descriptions, required form beans, and so on; all of these must be written, maintained, and kept in sync for every element of your project's user interface.

In contrast to this development paradigm, Echo offers a very efficient and sophisticated way to design and implement complex real world applications in pure Java, enabling one to fully leverage modern software engineering principles such as inheritance, component-oriented design, and refactoring, all without making a stop in the web tier. In addition, should the use of Java not be desired, most of the Echo API is available to JavaScript running entirely within the web browser.

# Features
Server-Side Java API
Echo provides a Java-based API for application development which requires absolutely no interaction with web tier technologies such as HTML, CSS, or JavaScript. To provide this capability, the Echo server-side framework makes use of a derivative of its own client-side framework (see below) to render the state of the server-side application remotely. Should additional rendering capabilities beyond those provided by the API be desired, advanced users can easily implement custom components in JavaScript that extend the framework.

Client-Side JavaScript API
The Echo framework is available to applications developed entirely within the web browser by using the client-side JavaScript API. The API of the client-side version is similar to the server-side framework and as such employs an API design that should be somewhat familiar to desktop application developers.

Delayed Batch Rendering The client application framework renders changes to its component hierarchy in batches. This strategy increases rendering efficiency and eliminates potential screen flickering issues.

Application Rendered Components (ARCs) Client-side component synchronization peers render components in the web browser by creating a container HTML element (e.g., a DIV) and then loading a new client-side Echo application within the DIV. For example, the Echo3Extras project's Rich Text Editor component uses this feature to display a menu bar, dialogs, color selection controls, toolbars and other user interface elements. By writing the Rich Text Editor as a client-side Echo application, a lot of time was saved and the editor is more feature rich as a result.

Java-like Development in JavaScript (CoreJS Library):
Echo is built atop the CoreJS JavaScript Library which provides cross-platform APIs for building JavaScript objects and interfacing with web browser features (CSS/DOM). The CoreJS Library imposes no dependencies on and makes no modifications to the existing JavaScript namespace, and yet it provides the following features:

Class-based JavaScript Inheritance (i.e., not prototype-based)
Abstract, Virtual, Static, and Final JavaScript Properties
Method References Tied to Class Instances
Automatic Client/Server Serialization:
Data objects sent between the client and server are automatically serialized between Java and JavaScript. The serialization architecture is extensible as serialization code for new object types may easily be added by advanced users.

Serializing components and commands between client and server is performed automatically using the built-in serialization architecture. The component developer only need specify which properties should be sent (for components, all local style properties are automatically sent).

User Experience Improvements
Improved Layout Engine: The third iteration of the Echo framework features an improved layout engine which can automatically size components to their content.
Reduced Bandwidth: Echo version 3 uses approximately 30-40% of the bandwidth of a comparable Echo version 2 application and much less compared to classic page-oriented applications.
Reduced Server Load: Due to the fact that the server is not rendering HTML for transmission to the client, it has quite a bit less work to do. The reduced bandwidth usage mentioned previously also means less CPU and memory utilization.
Keyboard Navigation / Focus Management: Focus among components is now fully tracked. Echo version 3 does away with the practice of using sequential tab index numbers to control focus order, instead presenting next/previous element focus events to components for a more intuitive user interface. Components that have multiple "focus points" particularly benefit from this feature.

# This version
Thsi version has been forked from https://github.com/echo3/echo3. I maintain it for my personnal and professionnal projects.

