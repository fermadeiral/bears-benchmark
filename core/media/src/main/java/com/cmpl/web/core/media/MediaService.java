package com.cmpl.web.core.media;

import com.cmpl.web.core.common.service.BaseService;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService extends BaseService<MediaDTO> {

  MediaDTO upload(MultipartFile multipartFile) throws Exception;

  InputStream download(String mediaName);

  MediaDTO findByName(String name);

}
