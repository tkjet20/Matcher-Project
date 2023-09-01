package com.real.matcher;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class MatcherImpl implements Matcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(MatcherImpl.class);
  private static final CSVParser csvParser = new CSVParserBuilder().withQuoteChar('"').build();

  private final List<Movie> movieList;
  private final List<Role> roleList;

  public MatcherImpl(CsvStream movieDb, CsvStream actorAndDirectorDb) {
    LOGGER.info("importing database");
    // TODO implement me

    // Have used Factory design pattern in the second commit. To see how I have implemented the design pattern
    // to make the code extensible please see my second commit
    // Then realized, I'm not allowed to alter the test case so code not as extensible as it could be.


    //Implementation logic
    // parse CsvStream, with the help of a helper method, to an object. And have a list of each object.

    int movieHeadersLength = movieDb.getHeaderRow().split(",").length;
    Map<String, Integer> movieHeaders = IntStream.range(0, movieHeadersLength).boxed().collect(Collectors.toMap(i -> movieDb.getHeaderRow().split(",")[i], Function.identity()));

    Function<String[], Movie> movieFunction = parts -> {
      Integer id = Integer.parseInt(parts[movieHeaders.get("id")]);
      String title = parts[movieHeaders.get("title")];
      Integer year = Integer.parseInt(parts[movieHeaders.get("year")]);

      return new Movie(id, title, year);
    };

    movieList = parseCsvData(movieDb, movieFunction, null);

    int rolesHeadersLength = actorAndDirectorDb.getHeaderRow().split(",").length;
    Map<String, Integer> rolesHeaders = IntStream.range(0, rolesHeadersLength).boxed().collect(Collectors.toMap(i -> actorAndDirectorDb.getHeaderRow().split(",")[i], Function.identity()));

    Function<String[], Role> roleFunction = parts -> {
      Integer movieId = Integer.parseInt(parts[rolesHeaders.get("movie_id")]);
      String name = parts[rolesHeaders.get("name")];
      String role = parts[rolesHeaders.get("role")];

      return new Role(movieId, name, role);
    };

    roleList = parseCsvData(actorAndDirectorDb, roleFunction, null);

    LOGGER.info("database imported");
  }

  @Override
  public List<IdMapping> match(DatabaseType databaseType, CsvStream externalDb) {
    // TODO implement me

    //Implementation logic
    // parse extId, title, and releaseYear from externalDb
    // check if stream of values url contains databaseType. If not, skip that string in the stream
    // check if movie exists in internalDb using the providers year and title.
    // if movie exists, then check if the idMapping already exists. if not then add it to the set. return the list of idMappings

    Map<String, Integer> externalHeaders = new HashMap<>();

    String[] externalHeaderArray = externalDb.getHeaderRow().split(",");

    for(int i=0; i<externalHeaderArray.length; i++){
      externalHeaders.put(externalHeaderArray[i], i);
    }

    Set<IdMapping> uniqueIdMappings = new HashSet<>();

    Function<String[], IdMapping> idMappingMapper = parts ->{
      String extId = parts[externalHeaders.get("MediaId")];
      String title = parts[externalHeaders.get("Title")];
      Integer releaseYear = parseYear(parts[externalHeaders.get("OriginalReleaseDate")]);

      Movie movie = findMovieByYearAndTitle(releaseYear, title);

      if(movie == null){
        return null;
      }

      IdMapping existingMapping = findExistingIdMapping(uniqueIdMappings, extId, movie.getId());

      if(existingMapping == null){
        uniqueIdMappings.add(new IdMapping(movie.getId(), extId));
      }

      return null;
    };

    parseCsvData(externalDb, idMappingMapper, databaseType.toString());

    return new ArrayList<>(uniqueIdMappings);
  }

  private <T> List<T> parseCsvData(CsvStream csvStream, Function<String[], T> mapper, String databaseName){
    int headersLength = csvStream.getHeaderRow().split(",").length;
    return csvStream.getDataRows().map(s -> {
      try{
        String[] parts = csvParser.parseLine(s);
        if(parts.length != headersLength){
          LOGGER.warn("Will be skipping this roles record due to invalid format: {}", s);
          return null;
        }

        T mappedObject;

        try{
          mappedObject = mapper.apply(parts);
        }catch (NumberFormatException e){
          LOGGER.warn("Will be skipping this roles record due to invalid format: {}", s);
          return null;
        }

        if(mappedObject instanceof Movie){
          Movie movie = (Movie) mappedObject;

          if(movie.getId() == null){
            LOGGER.warn("Skipping movie with missing id: {}", s);
            return null;
          }

          if(movie.getTitle() == null || movie.getTitle().isEmpty()){
            LOGGER.warn("Skipping movie with missing title: {}", s);
            return null;
          }

          if(movie.getYear() == null){
            LOGGER.warn("Skipping movie with missing year: {}", s);
            return null;
          }
        }

        if(mappedObject instanceof Role){
          Role role = (Role) mappedObject;

          if(role.getMovieId() == null){
            LOGGER.warn("Skipping role with missing movieId: {}", s);
            return null;
          }
        }

        if(mappedObject instanceof IdMapping){
          String url = parts[headersLength-1];
          if(!url.toLowerCase().contains(databaseName.toLowerCase())){
            LOGGER.warn("Skipping provider record with missing database name in url: {}", s);
            return null;
          }
        }

        return mappedObject;

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

  private int parseYear(String dateString){
    try{
      Date date = new SimpleDateFormat("M/d/yyyy hh:mm:ss a").parse(dateString);
      return Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
    } catch (ParseException | NumberFormatException e) {
      return -1;
    }
  }

  private Movie findMovieByYearAndTitle(int year, String title){
    return movieList.stream().filter(x -> x.getYear() == year && x.getTitle().equalsIgnoreCase(title))
            .findFirst()
            .orElse(null);
  }

  private IdMapping findExistingIdMapping(Set<IdMapping> mappings, String externalId, int internalId){
    return mappings.stream().filter(obj -> obj.getExternalId().equals(externalId) && obj.getInternalId() == internalId)
            .findFirst()
            .orElse(null);
  }

}
