# ISIN Tool

`isin-tool.sc` in the _tools_ directory reads potential ISINs from stdin, one
per line, and parses them. It will panic if any fail to parse. This can be used
as a simple bulk test of a file of purported ISINs to ensure there are no
malformed entries present.

As part of the `Isin` type's initial validation, this tool was run on a file of 5,273,047
unique ISINs produced by processing a file mapping LEIs to ISINs obtained from GLEIF. The
[GLEIF file](https://www.gleif.org/en/lei-data/lei-mapping/download-isin-to-lei-relationship-files)
is very large (the version from 2021-02-09 was about 170MB). Here are a few example records from
the beginning of the file (the first line is the header row with field names):

```sh
head -11 ISIN_LEI_20210209.csv | tail -10
S6XOOCT0IEG5ABCC6L87,US3137A3KN83
XZYUUT6IYN31D9K77X08,DE000JC86RE7
378900EB75D7D2C73323,ZAG000163650
254900EDYO1UYWLWP146,US12613N2027
549300DRQQI75D2JP341,US05531GQN42
S6XOOCT0IEG5ABCC6L87,US31394GAX16
K6Q0W1PS1L1O4IQL9C32,DE000SLA61X8
529900W18LQJJN6SJ336,DE000CL78501
S6XOOCT0IEG5ABCC6L87,US3137ASGH19
G5GSEF7VJP5I7OUK5573,US06741RAP64
```

You can use a command like this to subset just the ISINs:

```sh
sed -e 's/^.*,//' ISIN_LEI_20210209.csv \
  | grep -v '^ISIN$' \
  | sort | uniq | gzip -9 \
  > isins.txt.gz
```

This file was about 16.6MB for the version tested and contained over 5.2 million ISINs.

Having produced the file, it is now possible to run it through this tool. From the source
directory of this crate, you can run:

```sh
gzcat isins.txt.gz | tools/isin-tool.sc
```

And, output will be something like this:

```text
Read 5273047 values; 5273047 were valid ISINs and 0 were not.
```

If no bad values were found, the tool will exit with zero status, else non-zero.


## Fix mode

If you run with argument `--fix`, then any input ISINs that are only wrong due to incorrect
_Check Digit_ will be fixed. In this mode, every good and every fixable input ISIN is printed
to standard output.
