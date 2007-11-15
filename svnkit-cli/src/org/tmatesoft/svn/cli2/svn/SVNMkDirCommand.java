/*
 * ====================================================================
 * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.cli2.svn;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.cli2.SVNCommandUtil;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.SVNPath;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 */
public class SVNMkDirCommand extends SVNCommand {

    public SVNMkDirCommand() {
        super("mkdir", null);
    }
    
    public boolean isCommitter() {
        return true;
    }

    protected Collection createSupportedOptions() {
        Collection options = new LinkedList();
        options.add(SVNOption.QUIET);
        options.add(SVNOption.PARENTS);
        options = SVNOption.addLogMessageOptions(options);
        options = SVNOption.addAuthOptions(options);
        options.add(SVNOption.CONFIG_DIR);
        return options;
    }

    public void run() throws SVNException {
        List targets = getSVNEnvironment().combineTargets(getSVNEnvironment().getTargets());
        if (targets.isEmpty()) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.CL_INSUFFICIENT_ARGS));
        }
        boolean hasURLs = false;
        boolean hasPaths = false;
        for (Iterator ts = targets.iterator(); ts.hasNext();) {
            String targetName = (String) ts.next();
            if (!SVNCommandUtil.isURL(targetName)) {
                if (getSVNEnvironment().getMessage() != null || getSVNEnvironment().getFileData() != null || getSVNEnvironment().getRevisionProperties() != null) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.CL_UNNECESSARY_LOG_MESSAGE,
                            "Local, non-commit operations do not take a log message or revision properties");
                    SVNErrorManager.error(err);
                }
                hasPaths = true;
            } else {
                hasURLs = true;
            }
        }
        if (hasURLs && hasPaths) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.CL_ARG_PARSING_ERROR, "Specify either URLs or local paths, not both"));
        }
        if (hasURLs) {
            SVNCommitClient client = getSVNEnvironment().getClientManager().getCommitClient();
            if (!getSVNEnvironment().isQuiet()) {
                client.setEventHandler(new SVNNotifyPrinter(getSVNEnvironment()));
            }
            client.setCommitHandler(getSVNEnvironment());
            SVNURL[] urls = new SVNURL[targets.size()];
            for (int i = 0; i < targets.size(); i++) {
                String url = (String) targets.get(i);
                urls[i] = SVNURL.parseURIEncoded(url);
            }
            try {
                SVNCommitInfo info = client.doMkDir(urls, getSVNEnvironment().getMessage(), getSVNEnvironment().getRevisionProperties(), getSVNEnvironment().isParents());
                getSVNEnvironment().printCommitInfo(info);
            } catch (SVNException e) {
                SVNErrorMessage err = e.getErrorMessage();
                if (!getSVNEnvironment().isParents() && (err.getErrorCode() == SVNErrorCode.FS_NOT_FOUND || err.getErrorCode() == SVNErrorCode.RA_DAV_PATH_NOT_FOUND)) {
                    err = err.wrap("Try 'svn mkdir --parents' instead?");
                }
                SVNErrorManager.error(err);
            }
        } else {
            SVNWCClient client = getSVNEnvironment().getClientManager().getWCClient();
            if (!getSVNEnvironment().isQuiet()) {
                client.setEventHandler(new SVNNotifyPrinter(getSVNEnvironment()));
            }
            try {
                for (Iterator ts = targets.iterator(); ts.hasNext();) {
                    String targetName = (String) ts.next();
                    SVNPath target = new SVNPath(targetName);
                    client.doAdd(target.getFile(), false, true, false, true, false, getSVNEnvironment().isParents());
                }
            } catch (SVNException e) {
                SVNErrorMessage err = e.getErrorMessage();
                if (err.getErrorCode() == SVNErrorCode.IO_ERROR) {
                    err = err.wrap("Try 'svn mkdir --parents' instead?");
                } else if (!getSVNEnvironment().isParents() && (err.getErrorCode() == SVNErrorCode.IO_ERROR)) {
                    err = err.wrap("Try 'svn add' or 'svn add --non-recursive' instead?");
                }
                SVNErrorManager.error(err);
            }
        }
    }
}
