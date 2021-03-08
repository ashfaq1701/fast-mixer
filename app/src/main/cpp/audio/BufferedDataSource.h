//
// Created by Ashfaq Salehin on 26/2/2021 AD.
//

#ifndef FAST_MIXER_BUFFEREDDATASOURCE_H
#define FAST_MIXER_BUFFEREDDATASOURCE_H

#include "FileDataSource.h"
#include <vector>

using namespace std;

class BufferedDataSource : public FileDataSource {

public:
    static BufferedDataSource* newFromClipboard(vector<float>& clipboard, const AudioProperties targetProperties);

private:
    BufferedDataSource(bufferDataType data, size_t size, const AudioProperties properties);
};


#endif //FAST_MIXER_BUFFEREDDATASOURCE_H
