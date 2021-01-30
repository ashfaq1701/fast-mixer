//
// Created by Ashfaq Salehin on 30/1/2021 AD.
//

#include "GainAdjustment.h"

GainAdjustment::GainAdjustment(float gainFactorLog) : mGainFactorLog(gainFactorLog) {}

void GainAdjustment::synthesize(shared_ptr<FileDataSource> source) {
    auto data = source->getMainBufferData();
    auto bufferSize = source->getMainBufferSize();
    auto minValue = source->getMinSampleValue();
    auto maxValue = source->getMaxSampleValue();

    float* transformedBuffer = new float [bufferSize];

    float minBoundary = minValue < -1.0 ? minValue : -1.0;
    float maxBoundary = maxValue > 1.0 ? maxValue : 1.0;

    for (int i = 0; i < bufferSize; i++) {
        float newSampleValue = data[i] * pow(10.0f, mGainFactorLog * 0.05f);

        float clampedValue = 0.0;
        if (newSampleValue < minBoundary) {
            clampedValue = minBoundary;
        } else if (newSampleValue > maxBoundary) {
            clampedValue = maxBoundary;
        } else {
            clampedValue = newSampleValue;
        }

        transformedBuffer[i] = clampedValue;
    }

    source->setBackupBufferData(transformedBuffer, bufferSize);
}
