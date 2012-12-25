/**
 * Putlocker Android - Putlocker scraper for Android 
 *
 * Author: Julian Haldenby (j.haldenby@gmail.com)
 *
 *  This file is part of Putlocker Android.
 *
 * Putlocker Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Putlocker Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Putlocker Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.putlocker.upload.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.putlocker.upload.Constants;
import com.putlocker.upload.exception.PutlockerException;
import com.putlocker.upload.storage.AuthStorage;
import com.putlocker.upload.storage.PutlockerNode;
import com.putlocker.upload.storage.PutlockerNode.NodeType;

public class FileListRequest extends AuthenticatedRequest {
	private static Pattern filePattern;
	private static Pattern folderPattern;
	private final HttpContext _context;
	
	private PutlockerNode _folderHash = null;
	
	private HashMap<String,PutlockerNode> _nodes = new HashMap<String, PutlockerNode>();
	public FileListRequest(AuthStorage credentials, RequestCallback callback) {
		this(credentials,callback,null);
	}
	
	public FileListRequest(AuthStorage credentials,RequestCallback callback,PutlockerNode folderHash)
	{
		super(Constants.BASE_URL + Constants.FILE_LIST_URL + (folderHash != null ? "?folder="+folderHash.getNodeKey() : "" ),credentials,callback);
		_context = new BasicHttpContext();
		_folderHash = folderHash;
	}

	
	@Override
	protected HttpResponse doHttpQuery() throws IOException, PutlockerException
	{
		return super.doHttpQuery();		
	}

	@Override
	protected HttpContext getContext() {
		return _context;
	}

	@Override
	protected void processResponse(HttpResponse response) throws IOException,
			PutlockerException {
		super.processResponse(response);
		HttpEntity entity = response.getEntity();

		String responseText = getStringFromEntity(entity);
		
		/**
		 * Here we are going to extract all the files that we have
		*/
		if ( filePattern == null ) {
			filePattern = Pattern.compile("/file/([^\"]*?\")>([^<]*?<)");
		}
		
		Matcher matcher = filePattern.matcher(responseText);
		int parent = 0;
		
		if ( _folderHash != null ) {
			parent = _folderHash.getId();
		}
		
		while (matcher.find()) {
			String fileId = matcher.group(1);
			fileId = fileId.substring(0,fileId.length()-1);
			int index = matcher.end();
			String fileSize = "";
			String uploadDate = "";
			int endIndex = responseText.indexOf("</td>", index);
			
			String searchArea = responseText.substring(index, endIndex);
			
			int lastIndexOfBr = searchArea.lastIndexOf("<br>");
		
			if ( lastIndexOfBr != -1 ) {
				String brTag = searchArea.substring(lastIndexOfBr+4);
				brTag = brTag.trim();
				StringTokenizer tokener = new StringTokenizer(brTag, "|");
			
				
				if ( tokener.countTokens() == 3) {
					
					fileSize = tokener.nextToken();
					tokener.nextToken();
					uploadDate = tokener.nextToken();
				}
				
			}
			
			String fileFileName = matcher.group(2);
			fileFileName = fileFileName.substring(0,fileFileName.length()-1);
			_nodes.put(fileId,new PutlockerNode(NodeType.NODE_TYPE_FILE, fileFileName,fileId, fileSize, uploadDate,parent,false));
		}
		
		if (folderPattern == null ) {
			folderPattern = Pattern.compile("cp.php\\?folder=([^\"]*?\")([^>]*?>)([^<]*?<)");
		}
		
		Matcher folderMatch = folderPattern.matcher(responseText);
		
		while (folderMatch.find()) {
			String folderId = folderMatch.group(1);
			folderId = folderId.substring(0,folderId.length()-1);
			
			String folderName = folderMatch.group(3);
			folderName = folderName.substring(0,folderName.length()-1);
			_nodes.put(folderId,new PutlockerNode(NodeType.NODE_TYPE_FOLDER, folderName, folderId,"","", parent, false));
		}
		
		return;
	}
	
	
	public HashMap<String, PutlockerNode> getParsedNodes()
	{
		return _nodes;
	}

	public PutlockerNode getFileHash()
	{
		return _folderHash;
	}

}
