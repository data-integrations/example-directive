# Custom Directive or User Defined Directives (UDD)

User Defined Directives, also known as UDD, allow you to create custom functions to transform records within CDAP DataPrep or a.k.a Wrangler. CDAP comes with a comprehensive library of functions. There are however some omissions, and some specific cases for which UDDs are the solution.

UDDs, similar to User-defined Functions (UDFs) have a long history of usefulness in SQL-derived languages and other data processing and query systems.  While the framework can be rich in their expressiveness, there's just no way they can anticipate all the things a developer wants to do.  Thus, the custom UDF has become commonplace in our data manipulation toolbox. In order to support customization or extension, CDAP now has the ability to build your own functions for manipulating data.

Developing CDAP DataPrep UDDs by no means rocket science, and is an effective way of solving problems that could either be downright impossible, or does not meet your requirements or very akward to solve. 

## How to get started

A example for writing custom directives
