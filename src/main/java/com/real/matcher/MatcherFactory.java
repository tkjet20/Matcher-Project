package com.real.matcher;

public interface MatcherFactory{
    Matcher createFactory(Matcher.DatabaseType databaseType, Matcher.CsvStream moviesCsv, Matcher.CsvStream actorsAndDirectorsCsv);
}
