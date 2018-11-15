/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.viewer.admin.stripes;

import java.util.ArrayList;
import java.util.List;
import nl.b3p.viewer.config.app.Application;
import nl.b3p.viewer.config.app.ApplicationLayer;
import nl.b3p.viewer.config.app.Level;
import nl.b3p.viewer.config.app.StartLayer;
import nl.b3p.viewer.config.app.StartLevel;
import nl.b3p.viewer.util.SelectedContentCache;
import nl.b3p.viewer.util.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class ApplicationStartMapActionBeanTest extends TestUtil{
    private static final Log log = LogFactory.getLog(ApplicationStartMapActionBeanTest.class);
    private ApplicationStartMapActionBean instance = new ApplicationStartMapActionBean();
    private Application application;
    
    public ApplicationStartMapActionBeanTest() {
        String sc = "[{ \"id\" : \"3\", \"type\" : \"level\"}, { \"id\" : \"4\", \"type\" : \"level\"}, { \"id\" : \"5\", \"type\" : \"level\"},{ \"id\" : \"6\", \"type\" : \"level\"}]";
        instance.setSelectedContent("[]");
    //    instance.setSelectedContent(sc);
        instance.setCheckedLayersString("[]");
    }
   
    @Test
    public void testRemoveStartLayerFromMashup(){
        
        initData(true);
        application = entityManager.find(Application.class, 2L);
        try {
            int expectedStartLayerSize = application.getStartLayers().size();
            int expectedStartLevelSize = application.getStartLevels().size();

            ApplicationLayer al2 = entityManager.find(ApplicationLayer.class, testAppLayer.getId());
            
            Application mashup = application.createMashup("mashup", entityManager,false);
            entityManager.persist(mashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            instance.setApplication(mashup);
            
            JSONObject removeString = new JSONObject();
            removeString.put("type", "layer");
            removeString.put("id", testAppLayer.getId());
            JSONArray ar = new JSONArray();
            ar.put(removeString);
            instance.setRemovedRecordsString(ar.toString());
            instance.setReaddedLayersString("[]");
            
            instance.saveStartMap(entityManager);
            
            // test of mashup is verandert
            ApplicationLayer al = entityManager.find(ApplicationLayer.class, testAppLayer.getId());
            StartLayer sl = al.getStartLayers().get(mashup);
            assertNotNull(sl);
            assertTrue(sl.isRemoved());
            assertEquals(expectedStartLayerSize , mashup.getStartLayers().size());
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());
            
            // test of moederapplicatie NIET is verandert
            StartLayer slOrig = al.getStartLayers().get(application);
            assertNotNull(slOrig);
            assertTrue(slOrig.getSelectedIndex() != null && slOrig.getSelectedIndex() >= 0);
            assertTrue(!slOrig.isRemoved());
            assertEquals(expectedStartLayerSize, application.getStartLayers().size());
            assertEquals(expectedStartLevelSize, application.getStartLevels().size());
        }catch(Exception e){
            log.error ("fout",e);
            assert(false);
        }
    }
   
    @Test
    public void testRemoveStartLevelFromMashup(){
        
        initData(true);
        application = entityManager.find(Application.class, 1L);
        try {
            int expectedStartLayerSize = application.getStartLayers().size();
            int expectedStartLevelSize = application.getStartLevels().size();
            int expectedRootStartLevelSize = application.getRoot().getStartLevels().size() * 2;

            Application mashup = application.createMashup("mashup", entityManager,false);
            entityManager.persist(mashup);

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            instance.setApplication(mashup);
            
            JSONObject removeString = new JSONObject();
            removeString.put("type", "level");
            removeString.put("id", 3L);
            JSONArray ar = new JSONArray();
            ar.put(removeString);
            instance.setRemovedRecordsString(ar.toString());
            instance.setReaddedLayersString("[]");
            instance.saveStartMap(entityManager);
            
            // test of mashup is verandert
            Level l = entityManager.find(Level.class, 3L);
            StartLevel slMashup = l.getStartLevels().get(mashup);
            assertNotNull(slMashup);
            assertTrue(slMashup.isRemoved());
            assertEquals(expectedStartLayerSize , mashup.getStartLayers().size());
            assertEquals(expectedStartLevelSize, mashup.getStartLevels().size());

            assertEquals(expectedRootStartLevelSize, application.getRoot().getStartLevels().size());
            
            // test of moederapplicatie NIET is verandert
            StartLevel slOrig = l.getStartLevels().get(application);
            assertNotNull(slOrig);
            assertTrue(slOrig.getSelectedIndex() >= 0);
            assertFalse(slOrig.isRemoved());
            assertEquals(expectedStartLayerSize, application.getStartLayers().size());
            assertEquals(expectedStartLevelSize, application.getStartLevels().size());
            assertEquals(expectedRootStartLevelSize, application.getRoot().getStartLevels().size());
            
        }catch(Exception e){
            log.error (e);
            assert(false);
        }
    }
    
    @Test
    public void testRemoveStartLevelWithParent(){
        initData(true);
        application = entityManager.find(Application.class, 1L);
        try {
           
            instance.setApplication(application);
            
            /*
                Tree is:
                    Thema (4)
                        Groen (5)
                        Woonplaatsen (6)
            */
            // voeg level "Thema" (id = 4) toe aan startkaartbeeld 
            String selectedContent = "[{\"id\":\"4\",\"type\":\"level\"}]";
            instance.setSelectedContent(selectedContent);
            instance.setRemovedRecordsString(null);
            instance.setReaddedLayersString("[]");
            instance.setApplication(application);
            instance.saveStartMap(entityManager);
            
            entityManager.getTransaction().begin();
            // verwijder level Groen (id = 5) uit startkaartbeeld
            
            JSONObject removeString = new JSONObject();
            removeString.put("type", "level");
            removeString.put("id", 5L);
            JSONArray ar = new JSONArray();
            ar.put(removeString);
            instance = new ApplicationStartMapActionBean();
            instance.setReaddedLayersString("[]");
            instance.setApplication(application);
            instance.setRemovedRecordsString(ar.toString());
            
        
            instance.setSelectedContent(selectedContent);
            instance.setCheckedLayersString("[]");
            instance.saveStartMap(entityManager);
            
            // check of level Woonplaatsen (id=6) nog bestaat in startkaartbeeld
            instance.setLevelId("n4");
            JSONArray children = instance.loadSelectedLayers(entityManager);
            boolean found = false;
            for (Object object : children) {
                JSONObject obj = (JSONObject)object;
                if(obj.getString("id").equals("n6")){
                    found = true;
                }
                if(obj.getString("id").equals("n5")){
                    fail("Level should be in startkaartbeeld");
                }
            }
            assertTrue(found);
       }catch(Exception e){
            log.error (e);
            assert(false);
        }
    }
    
    @Test
    public void testWalkAppTreeForStartMapAfterRemovingSublevel(){
        testRemoveStartLevelWithParent(); // ok, maybe not that nice to call a different testmethod, but it creates the exact state we need.
        List selectedObjects = new ArrayList();
        Level rootlevel = application.getRoot();
        instance.walkAppTreeForStartMap(selectedObjects, rootlevel, application);
        selectedObjects.stream().map((selectedObject) -> {
            Integer lhsIndex;
            if (selectedObject instanceof StartLevel) {
                lhsIndex = ((StartLevel) selectedObject).getSelectedIndex();
            } else {
                lhsIndex = ((StartLayer) selectedObject).getSelectedIndex();
            }
            return lhsIndex;
        }).filter((lhsIndex) -> (lhsIndex.equals(-1))).forEachOrdered((_item) -> {
            fail("selected index should never be -1 in the startMap");
        });
    }
    
    @Test
    public void testWalkAppTreeForStartMap(){
        Application application = entityManager.find(Application.class, applicationId);
        List selectedObjects = new ArrayList();
        Level rootlevel = application.getRoot();
        instance.walkAppTreeForStartMap(selectedObjects, rootlevel, application);
        selectedObjects.stream().map((selectedObject) -> {
            Integer lhsIndex;
            if (selectedObject instanceof StartLevel) {
                lhsIndex = ((StartLevel) selectedObject).getSelectedIndex();
            } else {
                lhsIndex = ((StartLayer) selectedObject).getSelectedIndex();
            }
            return lhsIndex;
        }).filter((lhsIndex) -> (lhsIndex.equals(-1))).forEachOrdered((_item) -> {
            fail("selected index should never be -1 in the startMap");
        });
        assertEquals(3,selectedObjects.size());
    }
    
    @Test
    public void testWalkAppTreeForStartMapAfterMovingLevelIntoLevel(){
        Application application = entityManager.find(Application.class, applicationId);
        List selectedObjects = new ArrayList();
        Level rootlevel = application.getRoot();
        
        
        long themaIdLng = 6;
        String levelId = "n5";
        ApplicationTreeActionBean instance2 = new ApplicationTreeActionBean();
        instance2.moveLevel(levelId, "n" + themaIdLng ,entityManager); 
        
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        
        ApplicationStartMapActionBean.walkAppTreeForStartMap(selectedObjects, rootlevel, application);
        selectedObjects.stream().map((selectedObject) -> {
            Integer lhsIndex;
            if (selectedObject instanceof StartLevel) {
                lhsIndex = ((StartLevel) selectedObject).getSelectedIndex();
            } else {
                lhsIndex = ((StartLayer) selectedObject).getSelectedIndex();
            }
            return lhsIndex;
        }).filter((lhsIndex) -> (lhsIndex.equals(-1))).forEachOrdered((_item) -> {
            fail("selected index should never be -1 in the startMap");
        }); 
       assertEquals(2,selectedObjects.size());
    }
    
    @Test
    public void testLoadSelectedLayersAfterRemovingSublevel(){
        testRemoveStartLevelWithParent(); // ok, maybe not that nice to call a different testmethod, but it creates the exact state we need.
        instance.setLevelId("n4");
        JSONArray ar = instance.loadSelectedLayers(entityManager);
        for (Object object : ar) {
            JSONObject ob = (JSONObject)object;
            if(ob.getString("id").equals("n5")){
                fail("Layer should not be in startmap");
            }
        }
    }
    
    @Test
    public void testLoadSelectedLayersAfterRemovingSecondSublevel(){
        testRemoveStartLevelWithParent(); // ok, maybe not that nice to call a different testmethod, but it creates the exact state we need.
        
        JSONObject removeString = new JSONObject();
        removeString.put("type", "level");
        removeString.put("id", 6L);
        JSONArray ar = new JSONArray();
        ar.put(removeString);
        instance = new ApplicationStartMapActionBean();
        instance.setApplication(application);
        instance.setRemovedRecordsString(ar.toString());

        entityManager.getTransaction().begin();
        String selectedContent = "[]";
        instance.setSelectedContent(selectedContent);
        instance.setCheckedLayersString("[]");
        instance.setReaddedLayersString("[]");
        instance.saveStartMap(entityManager);
            
        instance.setLevelId("n4");
        JSONArray selectedLayers = instance.loadSelectedLayers(entityManager);
        for (Object object : selectedLayers) {
            JSONObject ob = (JSONObject)object;
            if(ob.getString("id").equals("n5")){
                fail("Layer should not be in startmap");
            }
            if(ob.getString("id").equals("n6")){
                fail("Layer should not be in startmap");
            }
        }
    }
    
    @Test
    public void testLoadResetSublayersAfterReaddingParent(){
        /*
        This test is for the situation when you have a tree:
            A (4)
                B(5)
                C(6)
        First, removing b and c, save, then remove a, save, add a. Result: A, B, and C should be present in the selectedLayers
            
        */
        // Verwijder b en c:
        testLoadSelectedLayersAfterRemovingSecondSublevel();
        
        // Verwijder a
         
        entityManager.getTransaction().begin();
        
        JSONObject removeObject = new JSONObject();
        removeObject.put("type", "level");
        removeObject.put("id", 4L);
        JSONArray ar = new JSONArray();
        ar.put(removeObject);
        instance = new ApplicationStartMapActionBean();
        instance.setApplication(application);
        instance.setRemovedRecordsString(ar.toString());
        instance.setSelectedContent("[]");
        instance.setCheckedLayersString("[]");
        instance.setReaddedLayersString("[]");
        instance.saveStartMap(entityManager);
        
        entityManager.getTransaction().begin();
        
        // Voeg a weer toe
        
        instance = new ApplicationStartMapActionBean();
        instance.setApplication(application);
        String selectedContent = "[{\"id\":\"4\",\"type\":\"level\"}]";
        instance.setSelectedContent(selectedContent);
        instance.setRemovedRecordsString("[]");
        instance.setCheckedLayersString("[]");
        instance.setReaddedLayersString("[]");
        instance.saveStartMap(entityManager);

        // Check of sublevels bestaan
        instance = new ApplicationStartMapActionBean();
        instance.setLevelId("n4");
        instance.setApplication(application);
        instance.setReaddedLayersString("[]");
        JSONArray selectedLayers = instance.loadSelectedLayers(entityManager);
        boolean foundA = false;
        boolean foundB = false;
        for (Object object : selectedLayers) {
            JSONObject ob = (JSONObject)object;
            if(ob.getString("id").equals("n5")){
                foundA = true;
            }
            if(ob.getString("id").equals("n6")){
                foundB = true;
            }
        }
         
        assertTrue(foundA);
        assertTrue(foundB);
    }
    
    @Test
    public void testSelectedContentCreationWithDeletedLevel(){
        testRemoveStartLevelWithParent();
        SelectedContentCache scc = new SelectedContentCache();
        JSONObject selectedContent = scc.createSelectedContent(application, false, false, false, entityManager);
        JSONObject levels = selectedContent.getJSONObject("levels");
        JSONObject level5 = levels.getJSONObject("5");
        assertTrue("Should have removed parameter in level in selectedContent", level5.optBoolean("removed",false));
    }
    
    @Test
    public void testSelectedContentCreationWithDeletedParent(){
        
        testLoadSelectedLayersAfterRemovingSecondSublevel();
        // remove parent
            
        entityManager.getTransaction().begin();
        
        JSONObject removeObject = new JSONObject();
        removeObject.put("type", "level");
        removeObject.put("id", 4L);
        JSONArray ar = new JSONArray();
        ar.put(removeObject);
        instance = new ApplicationStartMapActionBean();
        instance.setApplication(application);
        instance.setRemovedRecordsString(ar.toString());
        instance.setSelectedContent("[]");
        instance.setReaddedLayersString("[]");
        instance.setCheckedLayersString("[]");
        instance.saveStartMap(entityManager);
        
        entityManager.getTransaction().begin();
        
        
        SelectedContentCache scc = new SelectedContentCache();
        JSONObject selectedContent = scc.createSelectedContent(application, false, false, false, entityManager);
        JSONObject levels = selectedContent.getJSONObject("levels");
        
        JSONObject level4 = levels.getJSONObject("4");
        assertTrue("Should have removed parameter in level3 in selectedContent", level4.optBoolean("removed",false));
        
        JSONObject level5 = levels.getJSONObject("5");
        assertTrue("Should have removed parameter in level4 in selectedContent", level5.optBoolean("removed",false));
        
        JSONObject level6 = levels.getJSONObject("5");
        assertTrue("Should have removed parameter in level5 in selectedContent", level6.optBoolean("removed",false));
    }
}
