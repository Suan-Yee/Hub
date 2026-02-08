#!/usr/bin/env python3
"""
Simple Test Script for Create Post API (NO AUTHENTICATION)
Security is temporarily disabled for testing
"""

import requests
import json

BASE_URL = "http://localhost:8080"

def test_create_simple_post():
    """Test creating a simple text post"""
    print("\n" + "="*60)
    print("TEST 1: Creating Simple Text Post")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts"
    data = {
        "content": "Hello World! This is a test post without authentication. 🚀",
        "visibility": "public"
    }
    
    try:
        response = requests.post(url, data=data)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code in [200, 201]:
            result = response.json()
            print("✅ SUCCESS!")
            print(json.dumps(result, indent=2))
            post_id = result.get('data', {}).get('post', {}).get('id')
            return post_id
        else:
            print("❌ FAILED!")
            print(response.text)
            return None
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        return None

def test_create_post_with_tags():
    """Test creating a post with tags and mentions"""
    print("\n" + "="*60)
    print("TEST 2: Creating Post with Tags and Mentions")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts"
    data = {
        "content": "Testing tags and mentions: @john @alice #springboot #java #testing",
        "tags[0]": "springboot",
        "tags[1]": "java",
        "tags[2]": "testing",
        "mentions[0]": "john",
        "mentions[1]": "alice",
        "visibility": "public"
    }
    
    try:
        response = requests.post(url, data=data)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code in [200, 201]:
            result = response.json()
            print("✅ SUCCESS!")
            print(json.dumps(result, indent=2))
            post_id = result.get('data', {}).get('post', {}).get('id')
            return post_id
        else:
            print("❌ FAILED!")
            print(response.text)
            return None
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        return None

def test_create_poll_post():
    """Test creating a poll post"""
    print("\n" + "="*60)
    print("TEST 3: Creating Poll Post")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts"
    data = {
        "content": "Quick poll: What's your favorite programming language?",
        "poll.question": "Which do you prefer?",
        "poll.options[0]": "Java",
        "poll.options[1]": "Python",
        "poll.options[2]": "JavaScript",
        "poll.options[3]": "Go",
        "visibility": "public"
    }
    
    try:
        response = requests.post(url, data=data)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code in [200, 201]:
            result = response.json()
            print("✅ SUCCESS!")
            print(json.dumps(result, indent=2))
            post_id = result.get('data', {}).get('post', {}).get('id')
            return post_id
        else:
            print("❌ FAILED!")
            print(response.text)
            return None
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        return None

def test_get_post(post_id):
    """Test getting a post by ID"""
    if not post_id:
        print("\n⚠️  Skipping GET test - no post ID available")
        return
        
    print("\n" + "="*60)
    print(f"TEST 4: Getting Post by ID ({post_id})")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts/{post_id}"
    
    try:
        response = requests.get(url)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SUCCESS!")
            print(json.dumps(result, indent=2))
        else:
            print("❌ FAILED!")
            print(response.text)
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def test_get_feed():
    """Test getting the public feed"""
    print("\n" + "="*60)
    print("TEST 5: Getting Public Feed")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts/feed?page=0&size=10"
    
    try:
        response = requests.get(url)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SUCCESS!")
            posts_count = len(result.get('data', {}).get('posts', []))
            print(f"Retrieved {posts_count} posts")
            print(json.dumps(result, indent=2))
        else:
            print("❌ FAILED!")
            print(response.text)
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def test_update_post(post_id):
    """Test updating a post"""
    if not post_id:
        print("\n⚠️  Skipping UPDATE test - no post ID available")
        return
        
    print("\n" + "="*60)
    print(f"TEST 6: Updating Post ({post_id})")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts/{post_id}"
    data = {
        "content": "UPDATED: This post has been modified for testing purposes! ✨",
        "visibility": "public"
    }
    
    try:
        response = requests.put(url, data=data)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ SUCCESS!")
            print(json.dumps(result, indent=2))
        else:
            print("❌ FAILED!")
            print(response.text)
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def test_validation_error():
    """Test validation - missing required fields"""
    print("\n" + "="*60)
    print("TEST 7: Testing Validation (Expected to Fail)")
    print("="*60)
    
    url = f"{BASE_URL}/api/posts"
    data = {
        # Missing required 'content' field
        "visibility": "public"
    }
    
    try:
        response = requests.post(url, data=data)
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 400:
            print("✅ VALIDATION WORKING! (400 Bad Request as expected)")
            print(response.text)
        else:
            print("⚠️  Unexpected status code")
            print(response.text)
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def main():
    """Run all tests"""
    print("\n" + "="*60)
    print("SOCIAL MEDIA API - NO AUTH TESTING")
    print("Security is temporarily disabled")
    print("="*60)
    
    # Check if server is running
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        print(f"\n✅ Server is running at {BASE_URL}")
    except:
        print(f"\n❌ Server is not running at {BASE_URL}")
        print("Please start the application first with: mvn spring-boot:run")
        return
    
    # Run tests
    post_id1 = test_create_simple_post()
    post_id2 = test_create_post_with_tags()
    post_id3 = test_create_poll_post()
    
    # Use the first created post for further tests
    test_post_id = post_id1 or post_id2 or post_id3
    
    test_get_post(test_post_id)
    test_get_feed()
    test_update_post(test_post_id)
    test_validation_error()
    
    print("\n" + "="*60)
    print("TESTING COMPLETE!")
    print("="*60)
    print("\n📝 Note: Remember to re-enable security when done testing")
    print("   Uncomment the original security configuration in SecurityConfig.java")

if __name__ == "__main__":
    main()
