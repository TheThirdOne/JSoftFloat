JSoftFloat
----------

JSoftFloat aims to be a compliant implementation of the the [IEEE 754-2008 standard](http://ieeexplore.ieee.org/document/4610935/)
Its still a major work in progress though; there are major parts of the standard
which are not implemented yet. Decimal floats, square root and logrithms have not
been implemented yet. It is actively being developed though;  the goal is to
become mostly compliant by the end of 2017. 

## Bugs

If the standard disagrees with how JSoftFloat handles something, it is a bug. If
you submit an issue with an example and a quote from the standard, it should be
addressed in a timely manner. 

Suggestions from the specification that are not followed are not bugs, but a Pull
request which fufills a suggestion would be appreciated. 

## TODO

There are still a lot of things that are not done, but some of the large ones are:
  - Some type of property based fuzz testing
    - Commutativity of addition (single step)
    - Commutativity of multiplication (single step)
    - X + 0 = X
  - Testing against another software floating point library
    - Many of the base features can be tested against an IEEE 754 compliant implementation
    - One canidate would be [SoftFloat](http://www.jhauser.us/arithmetic/SoftFloat.html)
  - Decimal formats are currently unsupported
  - Support more widths of binary formats

 







