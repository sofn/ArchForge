package com.lesofn.archforge.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lesofn.archforge.common.error.example.TestErrorCodes;
import com.lesofn.archforge.common.error.example.TestSystemErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import com.lesofn.archforge.common.error.manager.TreeNode;
import com.lesofn.archforge.common.error.system.HttpCodes;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-10 12:28
 */
class ErrorManagerTest {

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void getAllErrorCodes() {
        TestSystemErrorCode.values();
        HttpCodes.values();
        TestErrorCodes.values();
        List<TreeNode> allErrorCodes = ErrorManager.getAllErrorCodes();
        assertEquals(2, allErrorCodes.size());

        // traverse the whole tree to prove no node throws
        for (TreeNode treeNode : allErrorCodes) {
            for (TreeNode node : java.util.Objects.requireNonNull(treeNode.getNodes())) {
                assertNotNull(node.getNodes());
            }
        }
    }
}
