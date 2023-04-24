/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.minion.taskset.worker;

import com.google.protobuf.Any;

public interface RetryableExecutor {
    /**
     * Initialize the executor for the workflow.
     *
     * @param handleRetryNeeded callback listening for disconnects in order to schedule reconnect attempts.  Only call
     *                         after a successful attempt() call.
     */
    void init(Runnable handleRetryNeeded);

    /**
     * Attempt the executor.  After success, needs to schedule a retry must be triggered by calling the disconnect
     * handler provided at init time.
     *
     * @throws Exception indicate failure of the attempt; another attempt is automatically scheduled.
     */
    void attempt(Any configuration) throws Exception;

    /**
     * Cancel the executor on shutdown of the workflow.
     */
    void cancel();
}
