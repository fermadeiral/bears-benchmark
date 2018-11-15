package nl.b3p.viewer.stripes;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.viewer.config.app.Application;
import nl.b3p.viewer.config.app.ApplicationLayer;
import nl.b3p.viewer.config.app.FileUpload;
import nl.b3p.viewer.config.security.Authorizations;
import nl.b3p.viewer.config.services.Layer;
import nl.b3p.web.stripes.ErrorMessageResolution;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;

@UrlBinding("/action/upload")
@StrictBinding
public class FileUploadActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(FileUploadActionBean.class);
    private ActionBeanContext context;
    public static final String DATA_DIR = "flamingo.data.dir";

    private List<FileBean> files;

    @Validate
    private ApplicationLayer appLayer;

    @Validate
    private Application application;

    @Validate
    private String type;

    @Validate
    private String fid;

    @Validate
    private FileUpload upload;

    // <editor-fold default-state="collapsed" desc="Getters and setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public ApplicationLayer getAppLayer() {
        return appLayer;
    }

    public void setAppLayer(ApplicationLayer appLayer) {
        this.appLayer = appLayer;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<FileBean> getFiles() {
        return files;
    }

    public void setFiles(List<FileBean> files) {
        this.files = files;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FileUpload getUpload() {
        return upload;
    }

    public void setUpload(FileUpload upload) {
        this.upload = upload;
    }

    // </editor-fold>

    @DefaultHandler
    public Resolution uploadFile() {
        JSONObject json = new JSONObject();
        String error = null;

        if(appLayer == null ) {
            error = "App layer or service not found";
        }

        if (error == null) {
            if (!Authorizations.isAppLayerWriteAuthorized(application, appLayer, context.getRequest(), Stripersist.getEntityManager())) {
                error = "Not authorized";
            }
        }
        if(error != null){
            json.put("success", false);
            json.put("message", error);
        }else {
            String datadir = context.getServletContext().getInitParameter(DATA_DIR);
            if (datadir.isEmpty()) {
                json.put("success", false);
                json.put("message", "Upload directory niet geconfigureerd. Neem contact op met de systeembeheerder.");
            } else {
                File dir = new File(datadir);
                if (dir.exists() && dir.canWrite()) {
                    Long time = System.currentTimeMillis();
                    EntityManager em = Stripersist.getEntityManager();
                    Layer layer = appLayer.getService().getLayer(appLayer.getLayerName(), em);
                    for (FileBean fb : files) {
                        String filename = fb.getFileName();
                        String extension = filename.substring(filename.lastIndexOf("."));
                        filename = filename.substring(0, filename.lastIndexOf("."));

                        File f = new File(dir, "uploads" + File.separator + appLayer.getLayerName() + "_" + fid + "_" + filename + "_" + time + extension);
                        try {
                            FileUtils.copyToFile(fb.getInputStream(), f);
                            if (!em.getTransaction().isActive()) {
                                em.getTransaction().begin();
                            }
                            FileUpload fu = new FileUpload();
                            fu.setCreatedAt(new Date());
                            fu.setFid(fid);
                            fu.setType_(type);
                            fu.setFilename(fb.getFileName());

                            fu.setMimetype(fb.getContentType());
                            fu.setLocation(f.getName());
                            fu.setSft(layer.getFeatureType());
                            em.persist(fu);
                            em.getTransaction().commit();
                        } catch (IOException e) {
                            log.error("Cannot write file", e);
                        }
                    }
                    json.put("success", true);
                } else {
                    json.put("success", false);
                    json.put("message", "Upload directory niet goed geconfigureerd: bestaat niet of kan niet schrijven. Neem contact op met de systeembeheerder.");
                }
            }
        }
        return new StreamingResolution("application/json", new StringReader(json.toString(4)));
    }

    public static JSONObject retrieveUploads(String fid, ApplicationLayer appLayer, EntityManager em, Application application, HttpServletRequest request) {
        JSONObject uploads = new JSONObject();
        String error = null;

        if(appLayer == null ) {
            error = "App layer or service not found";
        }

        if (error == null) {
            if (!Authorizations.isAppLayerReadAuthorized(application, appLayer, request, Stripersist.getEntityManager())) {
                error = "Not authorized";
            }
        }
        if(error == null) {


            Layer layer = appLayer.getService().getLayer(appLayer.getLayerName(), em);
            List<FileUpload> fups = em.createQuery("FROM FileUpload WHERE sft = :sft and fid = :fid", FileUpload.class)
                    .setParameter("sft", layer.getFeatureType()).setParameter("fid", fid).getResultList();

            for (FileUpload fup : fups) {
                if (!uploads.has(fup.getType_())) {
                    uploads.put(fup.getType_(), new JSONArray());
                }
                JSONArray ar = uploads.getJSONArray(fup.getType_());
                ar.put(fup.toJSON());
            }
        }
        return uploads;
    }

    public Resolution view() {
        final FileUpload up = upload;
        String error = null;
        if(appLayer == null ) {
            error = "App layer or service not found";
        }

        if (error == null) {
            if (!Authorizations.isAppLayerReadAuthorized(application, appLayer, context.getRequest(), Stripersist.getEntityManager())) {
                error = "Not authorized";
            }
        }
        if(error == null) {
            String datadir = context.getServletContext().getInitParameter(DATA_DIR);
            File dir = new File(datadir);
            File f = new File(dir, "uploads" + File.separator + up.getLocation());
            final FileInputStream fis;
            try {
                fis = new FileInputStream(f);

                StreamingResolution res = new StreamingResolution(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(f)) {
                    @Override
                    public void stream(HttpServletResponse response) throws Exception {
                        OutputStream out = response.getOutputStream();
                        IOUtils.copy(fis, out);
                        fis.close();
                    }
                };
                String name = up.getFilename();
                res.setFilename(name);
                res.setAttachment(false);
                return res;
            } catch (FileNotFoundException e) {
                log.error("Cannot retrieve file: ", e);
                return new ErrorMessageResolution("Cannot retrieve upload:" + e.getLocalizedMessage());
            }
        }else{
            log.error("User unauthorized: " + error);
            return new ErrorMessageResolution("User unauthorized: " + error);
        }
    }

    public Resolution removeUpload() {
        String error = null;

        JSONObject json = new JSONObject();
        json.put("uploadid", upload.getId());
        json.put("success", false);
        if(appLayer == null ) {
            error = "App layer or service not found";
        }

        if (error == null) {
            if (!Authorizations.isAppLayerWriteAuthorized(application, appLayer, context.getRequest(), Stripersist.getEntityManager())) {
                error = "Not authorized";
            }
        }
        if(error == null) {
            String datadir = context.getServletContext().getInitParameter(DATA_DIR);
            File dir = new File(datadir);
            File f = new File(dir, "uploads" + File.separator + upload.getLocation());
            EntityManager em = Stripersist.getEntityManager();
            if (f.exists()) {
                boolean deleted = f.delete();
                if (deleted) {
                    json.put("success", true);
                } else {
                    log.error("Kan bestand niet verwijderen: " + upload.getFilename());
                }
            } else {
                json.put("message", "Bestand bestaat niet");
            }
            em.remove(upload);
            em.getTransaction().commit();
        }else{
            json.put("message",error );
        }
        return new StreamingResolution("application/json", new StringReader(json.toString(4)));
    }
}
