#ifndef _OBJECT_H_
#define _OBJECT_H_

namespace Tizen{
namespace ArtikCloud {

class Object {
public:

	virtual char* toJson()
	{
	return NULL;
	}

	virtual ~Object()
	{
	}

	virtual void fromJson(char* jsonStr)
	{
	}
};

}
}
#endif /* _OBJECT_H_ */
