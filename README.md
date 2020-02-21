Paint_House.xml[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/cleberjamaral/autoOrgDesignProject) 

# GoOrg - From Goals to Organisations: automated organisation design method (Project)

This is the implementation of GoOrg ([PAAMS'19](https://link.springer.com/chapter/10.1007/978-3-030-24299-2_28) and [EMAS'19](http://cgi.csc.liv.ac.uk/~lad/emas2019/accepted/EMAS2019_paper_5.pdf) papers available) project. GoOrg is a method for automatic creation of organisations. This demand comes from complex projects of multiple systems working in a coordinated way in order to achieve mutual goals which are situations where Multi-Agent Systems are often applied. To design such systems it is necessary to define how they will work together, i.e., how they will be organised which can be a tough task for humans encharged for the design of the whole system. To track this problem and help humans to easier develop Multi-Agent Systems we propose GoOrg for generating the organisational structure of the system.

## Running and testing

To run it use `gradle run`
It should grab all dependencies of the project and run a very simple example which is a small goals tree asking for the most specialist roles, which is generating an organisational chart similar to the given goals tree, i.e., onde role to perform each goal. The last lines of the console output should say that the created organisation matches with the given proof. 

A graphviz representation of the input and of the output should be created in the folder `output/diagrams`. In \*nux systems, assuming it has `dot` installed, to generate PDF versions of these files type `.createPDF.sh`. The pdf files go to `output/graphs`.

To run other examples and with other cost function use the first argument to refer to a moise organisational description file and the second argument to select a cost function (see cost enumeration in javadocs for details). An example:

`gradle run --args="Paint_House.xml FLATTER"`

For other examples the proof will fail since it is set for the embedded example.

It is available an integrated test of all cost functions. This test proposes a simple goals tree and runs it with every cost function asserting if the output matches with the existing proofs. For generating this test enabling text outputs, execute:

`gradle test -i`

## Project status and roadmap

Currently, the project is using as input a goal decomposition tree with skills annotated on it. There are four possible transformations which is (i) for creating the root role, (ii) for creating subordinates, (iii) for joining a subordinate into an existing role, and (iv) for joining a pair into an existing role. Next version will add more annotation in order to improve the roles creation process which will allow the creation of coordinatiors, departments and so on.

## Eclipse developers
To add this project to eclipse use Import > Gradle > Existing Gradle Project.

## Further documentation

The [javadoc documentation](http://htmlpreview.github.io/?https://github.com/cleberjamaral/autoOrgDesignProject/blob/master/doc/apidoc/overview-tree.html) is available!
