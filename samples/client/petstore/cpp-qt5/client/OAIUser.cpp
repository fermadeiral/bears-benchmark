/**
 * OpenAPI Petstore
 * This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


#include "OAIUser.h"

#include "OAIHelpers.h"

#include <QJsonDocument>
#include <QJsonArray>
#include <QObject>
#include <QDebug>

namespace OpenAPI {

OAIUser::OAIUser(QString json) {
    init();
    this->fromJson(json);
}

OAIUser::OAIUser() {
    init();
}

OAIUser::~OAIUser() {
    this->cleanup();
}

void
OAIUser::init() {
    id = 0L;
    m_id_isSet = false;
    username = new QString("");
    m_username_isSet = false;
    first_name = new QString("");
    m_first_name_isSet = false;
    last_name = new QString("");
    m_last_name_isSet = false;
    email = new QString("");
    m_email_isSet = false;
    password = new QString("");
    m_password_isSet = false;
    phone = new QString("");
    m_phone_isSet = false;
    user_status = 0;
    m_user_status_isSet = false;
}

void
OAIUser::cleanup() {

    if(username != nullptr) { 
        delete username;
    }
    if(first_name != nullptr) { 
        delete first_name;
    }
    if(last_name != nullptr) { 
        delete last_name;
    }
    if(email != nullptr) { 
        delete email;
    }
    if(password != nullptr) { 
        delete password;
    }
    if(phone != nullptr) { 
        delete phone;
    }

}

OAIUser*
OAIUser::fromJson(QString json) {
    QByteArray array (json.toStdString().c_str());
    QJsonDocument doc = QJsonDocument::fromJson(array);
    QJsonObject jsonObject = doc.object();
    this->fromJsonObject(jsonObject);
    return this;
}

void
OAIUser::fromJsonObject(QJsonObject pJson) {
    ::OpenAPI::setValue(&id, pJson["id"], "qint64", "");
    
    ::OpenAPI::setValue(&username, pJson["username"], "QString", "QString");
    
    ::OpenAPI::setValue(&first_name, pJson["firstName"], "QString", "QString");
    
    ::OpenAPI::setValue(&last_name, pJson["lastName"], "QString", "QString");
    
    ::OpenAPI::setValue(&email, pJson["email"], "QString", "QString");
    
    ::OpenAPI::setValue(&password, pJson["password"], "QString", "QString");
    
    ::OpenAPI::setValue(&phone, pJson["phone"], "QString", "QString");
    
    ::OpenAPI::setValue(&user_status, pJson["userStatus"], "qint32", "");
    
}

QString
OAIUser::asJson ()
{
    QJsonObject obj = this->asJsonObject();
    QJsonDocument doc(obj);
    QByteArray bytes = doc.toJson();
    return QString(bytes);
}

QJsonObject
OAIUser::asJsonObject() {
    QJsonObject obj;
    if(m_id_isSet){
        obj.insert("id", QJsonValue(id));
    }
    if(username != nullptr && *username != QString("")){
        toJsonValue(QString("username"), username, obj, QString("QString"));
    }
    if(first_name != nullptr && *first_name != QString("")){
        toJsonValue(QString("firstName"), first_name, obj, QString("QString"));
    }
    if(last_name != nullptr && *last_name != QString("")){
        toJsonValue(QString("lastName"), last_name, obj, QString("QString"));
    }
    if(email != nullptr && *email != QString("")){
        toJsonValue(QString("email"), email, obj, QString("QString"));
    }
    if(password != nullptr && *password != QString("")){
        toJsonValue(QString("password"), password, obj, QString("QString"));
    }
    if(phone != nullptr && *phone != QString("")){
        toJsonValue(QString("phone"), phone, obj, QString("QString"));
    }
    if(m_user_status_isSet){
        obj.insert("userStatus", QJsonValue(user_status));
    }

    return obj;
}

qint64
OAIUser::getId() {
    return id;
}
void
OAIUser::setId(qint64 id) {
    this->id = id;
    this->m_id_isSet = true;
}

QString*
OAIUser::getUsername() {
    return username;
}
void
OAIUser::setUsername(QString* username) {
    this->username = username;
    this->m_username_isSet = true;
}

QString*
OAIUser::getFirstName() {
    return first_name;
}
void
OAIUser::setFirstName(QString* first_name) {
    this->first_name = first_name;
    this->m_first_name_isSet = true;
}

QString*
OAIUser::getLastName() {
    return last_name;
}
void
OAIUser::setLastName(QString* last_name) {
    this->last_name = last_name;
    this->m_last_name_isSet = true;
}

QString*
OAIUser::getEmail() {
    return email;
}
void
OAIUser::setEmail(QString* email) {
    this->email = email;
    this->m_email_isSet = true;
}

QString*
OAIUser::getPassword() {
    return password;
}
void
OAIUser::setPassword(QString* password) {
    this->password = password;
    this->m_password_isSet = true;
}

QString*
OAIUser::getPhone() {
    return phone;
}
void
OAIUser::setPhone(QString* phone) {
    this->phone = phone;
    this->m_phone_isSet = true;
}

qint32
OAIUser::getUserStatus() {
    return user_status;
}
void
OAIUser::setUserStatus(qint32 user_status) {
    this->user_status = user_status;
    this->m_user_status_isSet = true;
}


bool
OAIUser::isSet(){
    bool isObjectUpdated = false;
    do{
        if(m_id_isSet){ isObjectUpdated = true; break;}
        if(username != nullptr && *username != QString("")){ isObjectUpdated = true; break;}
        if(first_name != nullptr && *first_name != QString("")){ isObjectUpdated = true; break;}
        if(last_name != nullptr && *last_name != QString("")){ isObjectUpdated = true; break;}
        if(email != nullptr && *email != QString("")){ isObjectUpdated = true; break;}
        if(password != nullptr && *password != QString("")){ isObjectUpdated = true; break;}
        if(phone != nullptr && *phone != QString("")){ isObjectUpdated = true; break;}
        if(m_user_status_isSet){ isObjectUpdated = true; break;}
    }while(false);
    return isObjectUpdated;
}
}

