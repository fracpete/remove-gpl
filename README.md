# remove-gpl
Command-line tool that strips the GPL preamble from source codes files (eg when providing a commercial release).

## Command-line
TODO

## Pattern file example

Here is an example of the pattern file (in Java properties format) for text files:

```properties
# Template file for patterns

# the regular expression that the files must match in order to get processed
filepattern=.*\.txt

# the number of find/replace patterns (indexing starts at 1)
numpatterns=1

# the 1st find pattern
find1=.*This program is.*

# the 1st replace pattern
replace1=
```