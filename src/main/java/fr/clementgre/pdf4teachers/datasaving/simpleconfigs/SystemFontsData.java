package fr.clementgre.pdf4teachers.datasaving.simpleconfigs;


import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.utils.fonts.FontPaths;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.fonts.SystemFontsMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SystemFontsData extends SimpleConfig{
    
    private final int realSysFontsCount = SystemFontsMapper.getSystemFontNames().length;
    
    public SystemFontsData(){
        super("sysfonts_cache");
    }
    
    @Override
    protected void manageLoadedData(Config config){
        
        long fontsCount = config.getLong("systemFontsCount");
        if(fontsCount != realSysFontsCount){
            System.out.println("Updating system fonts indexing because fonts list length changed.");
            FontUtils.getSystemFontsMapper().loadFontsFromSystemFiles();
            return;
        }
        
        ArrayList<FontPaths> fontPathss = new ArrayList<>();
        
        for(Map.Entry<String, Object> entry : config.getSection("systemFontsCache").entrySet()){
            if(entry.getValue() instanceof HashMap map){
                String family = entry.getKey();
                FontPaths fontPaths = new FontPaths(family);
                fontPaths.deSerialize(map);
                fontPathss.add(fontPaths);
            }
        }
        
        if(fontPathss.isEmpty()){
            FontUtils.getSystemFontsMapper().loadFontsFromSystemFiles();
        }else{
            FontUtils.getSystemFontsMapper().loadFontsFromCache(fontPathss);
        }
        
    }
    
    @Override
    protected void unableToLoadConfig(){
        FontUtils.getSystemFontsMapper().loadFontsFromSystemFiles();
    }
    
    @Override
    protected void addDataToConfig(Config config){
        HashMap<String, Object> systemFontsCache = new HashMap<>();
        for(FontPaths font : FontUtils.getSystemFonts()){
            systemFontsCache.put(font.getName(), font.serialize());
        }
        
        config.set("systemFontsCount", realSysFontsCount);
        config.set("systemFontsCache", systemFontsCache);
    }
}
