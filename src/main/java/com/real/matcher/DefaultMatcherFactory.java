package com.real.matcher;

public class DefaultMatcherFactory implements MatcherFactory{
    @Override
    public Matcher createFactory(Matcher.DatabaseType databaseType, Matcher.CsvStream moviesCsv, Matcher.CsvStream actorsAndDirectorsCsv) {
        switch (databaseType){
            case XBOX:
                return new MatcherImpl(moviesCsv, actorsAndDirectorsCsv);
            default:
                throw new IllegalArgumentException("Unsupported database type is passed {} " + databaseType.name());
        }
    }
}
