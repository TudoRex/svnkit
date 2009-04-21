/*
 * ====================================================================
 * Copyright (c) 2004-2009 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.test.tests.merge.ext;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNCopyTask;
import org.tmatesoft.svn.core.wc.SVNEditorAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.test.wc.SVNWCDescriptor;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class MergeRenamedDirSourceTest1 extends MergeRenamedDirSourceTest {

    public ISVNTestExtendedMergeCallback getFeatureModeCallback() {
        return new FeatureModeCallback();
    }

    public ISVNTestExtendedMergeCallback getReleaseModeCallback() {
        return new ReleaseModeCallback();
    }

    protected void doMerge(long startRevision, long endRevision) throws SVNException {
        mergeLastRevisions(getBranch(), getTrunkWC(), endRevision - startRevision, SVNDepth.INFINITY, false, false);
    }

// ###############  FEATURE MODE  ###################

    private class FeatureModeCallback implements ISVNTestExtendedMergeCallback {

        public void prepareMerge(SVNURL source, File target) throws SVNException {
        }

        public SVNCopyTask getTargetCopySource(SVNURL sourceUrl, long sourceRevision, long sourceMergeFromRevision, long sourceMergeToRevision, SVNURL targetUrl, long targetRevision) throws SVNException {
            if (sourceUrl.getPath().endsWith("branch/B/file3")) {
                SVNCopySource source = new SVNCopySource(SVNRevision.create(targetRevision), SVNRevision.create(targetRevision), getTrunk().appendPath("A/file", false));
                return SVNCopyTask.create(source, true);
            }
            return null;
        }

        public SVNURL[] getTrueMergeTargets(SVNURL sourceUrl, long sourceRevision, long sourceMergeFromRevision, long sourceMergeToRevision, SVNURL targetUrl, long targetRevision, SVNEditorAction action) throws SVNException {
            if (action == SVNEditorAction.DELETE && sourceUrl.getPath().endsWith("branch/B/file2")) {
                return new SVNURL[0];
            }
            if (sourceUrl.getPath().endsWith("branch/B/file3")) {
                return new SVNURL[]{getTrunk().appendPath("A/file3", false)};
            }
            return null;
        }

        public SVNURL transformLocation(SVNURL sourceUrl, long sourceRevision, long targetRevision) throws SVNException {
            if (sourceUrl.getPath().endsWith("branch/B/file3") && sourceRevision == getMyEndRevision()) {
                return getBranch().appendPath("B/file2", false);
            }
            return null;
        }

        public SVNWCDescriptor getExpectedState() throws SVNException {
            return null;
        }
    }

// ###############  RELEASE MODE  ###################

    private class ReleaseModeCallback implements ISVNTestExtendedMergeCallback {

        public SVNCopyTask getTargetCopySource(SVNURL sourceUrl, long sourceRevision, long sourceMergeFromRevision, long sourceMergeToRevision, SVNURL targetUrl, long targetRevision) {
            return null;
        }

        public SVNURL[] getTrueMergeTargets(SVNURL sourceUrl, long sourceRevision, long sourceMergeFromRevision, long sourceMergeToRevision, SVNURL targetUrl, long targetRevision, SVNEditorAction action) throws SVNException {
            if (action == SVNEditorAction.DELETE && sourceUrl.getPath().endsWith("branch/B/file2")) {
                return new SVNURL[0];
            }
            if (sourceUrl.getPath().endsWith("branch/B/file3")) {
                return new SVNURL[]{getTrunk().appendPath("A/file", false)};
            }
            return null;
        }

        public SVNURL transformLocation(SVNURL sourceUrl, long sourceRevision, long targetRevision) throws SVNException {
            if (sourceUrl.getPath().endsWith("branch/B/file3") && sourceRevision == getMyEndRevision()) {
                return getBranch().appendPath("B/file2", false);
            }
            return null;
        }

        public void prepareMerge(SVNURL source, File target) throws SVNException {
        }

        public SVNWCDescriptor getExpectedState() throws SVNException {
            return null;
        }
    }
}