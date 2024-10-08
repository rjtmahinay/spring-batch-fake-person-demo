/**
 * MIT License
 * Copyright (c) 2024 Tristan Mahinay
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * spring-batch-meetup-demo
 *
 * @author rjtmahinay
 * 2024
 */
package com.rjtmahinay.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

/**
 * Listener for Chunk events.
 *
 */
@Component
@Slf4j
public class ChunkListener implements org.springframework.batch.core.ChunkListener {

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("Chunk finished: {}", context.getStepContext().getStepName());

        log.info("Chunk read: {} and written: {}",
                 context.getStepContext().getStepExecution().getReadCount()
                ,context.getStepContext().getStepExecution().getWriteCount());
    }
}
