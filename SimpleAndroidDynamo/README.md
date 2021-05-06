# Simple Dynamo

Programming assignment 4 in UB CSE 486 taught by Ethan Blanton

## Sources

Multiple basics of this code have been taken from previous programming assignments in this course.
- ClientTask: basis taken from PA2B implementation, which is a modified version of the ClientTask from PA1
  to use my Message class and to be more generic.

- InsertTask: based off of ClientTask however takes advantage of 2-way sockets and specifically
  returns a string for the purpose of insert method

- QueryTask: based off of ClientTask however takes advantage of 2-way sockets and returns a cursor
  to be used in query method

- ServerTask: basis also taken from PA2B implementation, which again is a modified version of the
  ClientTask from PA1 to use my Message class and to be more generic.

- SocketUtils: taken from PA2B with some slight modifications. Socket reading and writing methods
  influenced by Android Developer and Oracle java docs for Socket class and Writer classes.

- Message: Abstract class and all subclasses are original.

- MessageType: Original, basically used to serialize the class type across communication.

- SimpleDynamoActivity: Basic class given in template code. Additions to layout and UI functionality
  given influenced by experience developing Android in the past (multiple apps and read books such
  as Big Nerd Ranch Guide Android). Taken from my implementation in PA3

- SimpleDynamoProvider: Code for accessing (query, delete, insert) files locally in heavily influenced
  by Android Developer. Specifically docs on ContextWrapper for deleting document, Cursor and
  MatrixCursor for queries and inserts.

- Node: Just holds node info and a couple usedful helper methods

- NodeGroup: Reperesents a group of nodes and contians helper methods. Influenced by the Amazon
  Dynamo paper

The Amazon Dynamo paper and the architecture it describes is the main source for network design
on this project
