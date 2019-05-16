# remove-gpl
Command-line tool that strips the GPL preamble from source codes files (eg when providing a commercial release).

## Command-line
```
usage: com.github.fracpete.removegpl.Main
       [-h] --dir dir [--recursive] [--no-default-patterns]
       [--pattern-file PATTERNFILE [PATTERNFILE ...]] [--verbose]
       [--dry-run]

named arguments:
  -h, --help             show this help message and exit
  --dir dir              The input directory with the source code files.
  --recursive            Whether to process files recursively.
  --no-default-patterns  Whether  to  disable  the   use   of  the  default
                         patterns.
  --pattern-file PATTERNFILE [PATTERNFILE ...]
                         The pattern files to apply  (eg when not using the
                         built-in ones or additional ones).
  --verbose              Whether to process files in verbose mode.
  --dry-run              Files don't get updated when modified.
```

## Default pattern files

The following pattern files are built in:

* [java_jflex_cup.props](src/main/resources/com/github/fracpete/removegpl/java_jflex_cup.props) 
* [python.props](src/main/resources/com/github/fracpete/removegpl/python.props) 


## Pattern file format

Here is the [format of the pattern files](src/main/resources/com/github/fracpete/removegpl/template.props) 
(Java properties format):

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

## Maven

Add the following artifact to your dependencies of your `pom.xml`:

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>remove-gpl</artifactId>
      <version>0.0.2</version>
    </dependency>
```
