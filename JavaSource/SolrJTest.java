import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;

public class SolrJTest {
	public static void main(String[] args) throws Exception {
		long begin = System.currentTimeMillis();
//		upload();
//		query();
//		update();
//		get();
//		createCore("test22");
//		SolrJTest.indexFolder("E:\\co-drive\\Drive\\cyberobject\\ntelagent");
		SolrJTest.indexFolder("E:\\co-drive\\Student.xlsx");
		System.out.println(System.currentTimeMillis() - begin);
	}
	
	public static void query() throws Exception, IOException{
		SolrClient server = new HttpSolrClient("http://localhost:8087/solr/test1");
		SolrQuery query = new SolrQuery();
//		query.setQuery("id:\"E:\\\\co-drive\\\\Close_POTS_0225.vsd\"");
		query.setQuery("org:cyber");
		query.set("fl", "id,org,app");
//		query.set(CommonParams.WT, "json");
		QueryResponse response = server.query(query);
		SolrDocumentList list = response.getResults();
		for(SolrDocument sd : list){
			System.out.println(sd.getFieldValue("id") + "---" +sd.getFieldValue("org") + "---" +sd.getFieldValue("app"));
		}
	}
	
	public static void upload() throws Exception{
		SolrClient server = new HttpSolrClient("http://localhost:8087/solr/test22");
		String urlSuffix = "/update/extract";
		String filePath = "E:\\co-drive\\Student.xlsx";
		ContentStreamUpdateRequest req = new ContentStreamUpdateRequest(urlSuffix);
		req.addFile(new File(filePath), "application/octet-stream");
		req.setParam("literal.id", filePath);
		req.setParam("literal.filepath", filePath);
		req.setParam("literal.org", "cyber");
		req.setParam("literal.app", "lingtao");
		req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		NamedList<Object> result = server.request(req);
		server.close();
		System.out.println("Result: " + result);
	}
	
	public static void update() throws Exception{
		SolrClient server = new HttpSolrClient("http://localhost:8087/solr/test22");
//		String id = "E:\\co-drive\\Student.xlsx";
//		UpdateRequest ur = new UpdateRequest();
////		ur.deleteById("E:\\co-drive\\testrex.rex");
//		ur.deleteByQuery("id:\""+id.replaceAll("\\\\", "\\\\\\\\") +"\"");
//		ur.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
//		NamedList<Object> result = server.request(ur);
//		System.out.println("Result: " + result);
		
		UpdateRequest ur = new UpdateRequest();
//		ur.deleteById("E:\\co-drive\\testrex.rex");
		ur.deleteByQuery("org:cyber*");
		ur.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		NamedList<Object> result = server.request(ur);
		System.out.println("Result: " + result);
	}
	
	public static void get() throws Exception{
		SolrClient server = new HttpSolrClient("http://localhost:8080/solr/cyberobject");
		SolrDocument sd = server.getById("3d3a89af-80b8-4c9a-b039-3646c54a3316");
		System.out.println(sd.getFieldValue("id") + "---" +sd.getFieldValue("resourcename"));
	}
	
	public static void createCore(String coreName) throws SolrServerException, IOException{
		SolrClient server = new HttpSolrClient("http://localhost:8087/solr");
		CoreAdminResponse response = CoreAdminRequest.createCore(coreName, coreName, server);
		System.out.println(response);
	}
	
	public static void indexFolder(String filePath){
		FileFilter filter = new FileFilter(){
			@Override
			public boolean accept(File arg0) {
				String path = arg0.getAbsolutePath();
				if(arg0.isDirectory()){
					return true;
				}
				Pattern pattern = Pattern.compile(".*\\.((rex)|(vsd)|(xls)|(xlsx)|(docx)|(xml)|(wsdl)|(xsd))$", Pattern.CASE_INSENSITIVE);
				Matcher mather = pattern.matcher(path);
				if(mather.matches()){
					return true;
				}
				return false;
			}
		};
		
		File file = new File(filePath);
		if(file.isDirectory()){
			File[] subfiles = file.listFiles(filter);
			for(File subfile: subfiles){
				if(subfile.exists() && subfile.isDirectory()){
					indexFolder(subfile.getAbsolutePath());
				}else{
					indexFile(subfile.getAbsolutePath());
				}
			}
		}else{
			indexFile(filePath);
		}
	}
	
	public static void indexFile(String filePath){
		String org = null;
		String app = null;
		SolrClient server = null;
		try{
			File uploadFile = new File(filePath);
			
			// /opt/cyber/cocdev/apache-tomcat-7.0.27_17051/cyberobject/apps/co-drive/Drive/cyberobject/test
			//will cut ....../co-drive/Drive and split the path with "/"
			//take first of the String[] as org, second as app
			Pattern pattern = Pattern.compile(".*co-drive[\\\\/]{1}Drive[\\\\/]{1}.*");
			Matcher mather = pattern.matcher(filePath);
			//if this is  from co-drive, need find the org and app
			if(mather.matches()){
				String orgAppPath = filePath.replaceAll(".*co-drive[\\\\/]{1}Drive[\\\\/]{1}", "");
				String[] paths;
				if(orgAppPath.indexOf("\\") >= 0){
					paths = orgAppPath.split("\\\\");
				}else{
					paths = orgAppPath.split("/");
				}
				if(paths.length >= 1){
					org = paths[0];
				}
				if(paths.length >= 2){
					app = paths[1];
				}
				
				if(filePath.endsWith(org + File.separator + app + File.separator + uploadFile.getName())){
					System.out.println(filePath + " under " + org + "/" + app + " no need to index it.");
					return;
				}
				
				//files under org or app folder no need to index 
				if(filePath.endsWith(org + File.separator + app + File.separator + uploadFile.getName())
						|| filePath.endsWith(org + File.separator + uploadFile.getName())){
					System.out.println(filePath + " under " + org + "/" + app + " no need to index it.");
					return;
				}
				
				//files under debug and term folder no need to index 
				if(filePath.indexOf("debug" + File.separator) >=0
						||filePath.indexOf("term" + File.separator) >=0){
					System.out.println(filePath + " under " + org + "/" + app + " no need to index it.");
					return;
				}
			}
			
			server = new HttpSolrClient("http://localhost:8087/solr/test22");
			String urlSuffix = "/update/extract";
			ContentStreamUpdateRequest req = new ContentStreamUpdateRequest(urlSuffix);
			req.addFile(uploadFile, "application/octet-stream");
			req.setParam("literal.id", filePath);
			req.setParam("literal.filepath", filePath);
			req.setParam("literal.org", org==null?"Null":org);
			req.setParam("literal.app", app==null?"Null":app);
			req.setParam("literal.filename", uploadFile.getName());
			req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
			NamedList<Object> result = server.request(req);
			server.close();
			System.out.println("Index file : " + filePath + " for " + org + "---" + app + " success.");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Failed to index file : " + filePath + " for " + org + "---" + app);
		}finally{
			if(server != null){
				try {
					server.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
