<?php
/**
 * UserApiInterfaceTest
 * PHP version 5
 *
 * @category Class
 * @package  Swagger\Server\Tests\Api
 * @author   Swagger Codegen team
 * @link     https://github.com/swagger-api/swagger-codegen
 */

/**
 * Swagger Petstore
 *
 * This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 * OpenAPI spec version: 1.0.0
 * Contact: apiteam@swagger.io
 * Generated by: https://github.com/swagger-api/swagger-codegen.git
 *
 */

/**
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen
 * Please update the test case below to test the endpoint.
 */

namespace Swagger\Server\Tests\Api;

use Swagger\Server\Configuration;
use Swagger\Server\ApiClient;
use Swagger\Server\ApiException;
use Swagger\Server\ObjectSerializer;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

/**
 * UserApiInterfaceTest Class Doc Comment
 *
 * @category Class
 * @package  Swagger\Server\Tests\Api
 * @author   Swagger Codegen team
 * @link     https://github.com/swagger-api/swagger-codegen
 */
class UserApiInterfaceTest extends WebTestCase
{

    /**
     * Setup before running any test cases
     */
    public static function setUpBeforeClass()
    {
    }

    /**
     * Setup before running each test case
     */
    public function setUp()
    {
    }

    /**
     * Clean up after running each test case
     */
    public function tearDown()
    {
    }

    /**
     * Clean up after running all test cases
     */
    public static function tearDownAfterClass()
    {
    }

    /**
     * Test case for createUser
     *
     * Create user.
     *
     */
    public function testCreateUser()
    {
        $client = static::createClient();

        $path = '/user';

        $crawler = $client->request('POST', $path);
    }

    /**
     * Test case for createUsersWithArrayInput
     *
     * Creates list of users with given input array.
     *
     */
    public function testCreateUsersWithArrayInput()
    {
        $client = static::createClient();

        $path = '/user/createWithArray';

        $crawler = $client->request('POST', $path);
    }

    /**
     * Test case for createUsersWithListInput
     *
     * Creates list of users with given input array.
     *
     */
    public function testCreateUsersWithListInput()
    {
        $client = static::createClient();

        $path = '/user/createWithList';

        $crawler = $client->request('POST', $path);
    }

    /**
     * Test case for deleteUser
     *
     * Delete user.
     *
     */
    public function testDeleteUser()
    {
        $client = static::createClient();

        $path = '/user/{username}';
        $pattern = '{username}';
        $data = $this->genTestData('[a-z0-9]+');
        $path = str_replace($pattern, $data, $path);

        $crawler = $client->request('DELETE', $path);
    }

    /**
     * Test case for getUserByName
     *
     * Get user by user name.
     *
     */
    public function testGetUserByName()
    {
        $client = static::createClient();

        $path = '/user/{username}';
        $pattern = '{username}';
        $data = $this->genTestData('[a-z0-9]+');
        $path = str_replace($pattern, $data, $path);

        $crawler = $client->request('GET', $path);
    }

    /**
     * Test case for loginUser
     *
     * Logs user into the system.
     *
     */
    public function testLoginUser()
    {
        $client = static::createClient();

        $path = '/user/login';

        $crawler = $client->request('GET', $path);
    }

    /**
     * Test case for logoutUser
     *
     * Logs out current logged in user session.
     *
     */
    public function testLogoutUser()
    {
        $client = static::createClient();

        $path = '/user/logout';

        $crawler = $client->request('GET', $path);
    }

    /**
     * Test case for updateUser
     *
     * Updated user.
     *
     */
    public function testUpdateUser()
    {
        $client = static::createClient();

        $path = '/user/{username}';
        $pattern = '{username}';
        $data = $this->genTestData('[a-z0-9]+');
        $path = str_replace($pattern, $data, $path);

        $crawler = $client->request('PUT', $path);
    }

    protected function genTestData($regexp)
    {
        $grammar  = new \Hoa\File\Read('hoa://Library/Regex/Grammar.pp');
        $compiler = \Hoa\Compiler\Llk\Llk::load($grammar);
        $ast      = $compiler->parse($regexp);
        $generator = new \Hoa\Regex\Visitor\Isotropic(new \Hoa\Math\Sampler\Random());

        return $generator->visit($ast); 
    }
}
