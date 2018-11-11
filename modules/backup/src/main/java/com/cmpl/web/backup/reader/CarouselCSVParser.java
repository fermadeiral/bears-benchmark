package com.cmpl.web.backup.reader;

import com.cmpl.web.backup.writer.DataManipulator;
import com.cmpl.web.core.models.Carousel;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public class CarouselCSVParser extends CommonParser<Carousel> {

  public CarouselCSVParser(DateTimeFormatter dateFormatter,
      DataManipulator<Carousel> dataManipulator,
      String backupFilePath) {
    super(dateFormatter, dataManipulator, backupFilePath);
  }

  @Override
  protected Carousel parseEntity(CSVRecord record) {
    Carousel carouselParsed = new Carousel();

    List<Field> fieldsToParse = getFields(carouselParsed.getClass());

    parseObject(record, carouselParsed, fieldsToParse);

    return carouselParsed;
  }

  @Override
  public String getParserName() {
    return "carousels";
  }

}
