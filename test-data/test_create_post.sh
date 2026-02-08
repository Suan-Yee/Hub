#!/bin/bash

##############################################
# Social Media API Test Script
# Tests the Create Post endpoint with various scenarios
##############################################

# Configuration
BASE_URL="http://localhost:8080"
USERNAME="johndoe"
PASSWORD="password123"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Function to login and get JWT token
login() {
    echo ""
    echo "================================================"
    print_info "Logging in as $USERNAME..."
    echo "================================================"
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")
    
    # Extract token from response (adjust based on your response format)
    TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        print_error "Login failed. Please check your credentials."
        echo "Response: $RESPONSE"
        exit 1
    fi
    
    print_success "Login successful! Token obtained."
    echo ""
}

# Function to create a post
create_post() {
    local test_name=$1
    shift
    
    echo "================================================"
    print_info "Test: $test_name"
    echo "================================================"
    
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/posts" \
        -H "Authorization: Bearer $TOKEN" \
        "$@")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
        POST_ID=$(echo $BODY | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        print_success "Post created successfully! ID: $POST_ID"
    else
        print_error "Post creation failed. HTTP $HTTP_CODE"
        echo "$BODY" | head -c 500
    fi
    
    echo ""
}

# Main execution
echo ""
echo "###############################################"
echo "#  SOCIAL MEDIA API - CREATE POST TESTS      #"
echo "###############################################"
echo ""

# Check if curl is available
if ! command -v curl &> /dev/null; then
    print_error "curl is not installed. Please install curl and try again."
    exit 1
fi

# Login
login

# Test 1: Simple Text Post
create_post "Simple Text Post" \
    -F "content=Hello World! This is my first post on this social network. 🚀" \
    -F "visibility=public"

# Test 2: Post with Tags and Mentions
create_post "Post with Tags and Mentions" \
    -F "content=Hey @john and @alice! Check out my new project about #springboot #java #webdev" \
    -F "tags[0]=springboot" \
    -F "tags[1]=java" \
    -F "tags[2]=webdev" \
    -F "mentions[0]=john" \
    -F "mentions[1]=alice" \
    -F "visibility=public"

# Test 3: Poll Post
create_post "Poll Post" \
    -F "content=What's your favorite programming language? Let me know in the comments!" \
    -F "poll.question=Which programming language do you prefer?" \
    -F "poll.options[0]=Java" \
    -F "poll.options[1]=Python" \
    -F "poll.options[2]=JavaScript" \
    -F "poll.options[3]=Go" \
    -F "tags[0]=programming" \
    -F "tags[1]=poll" \
    -F "visibility=public"

# Test 4: Group Post
create_post "Group Post" \
    -F "content=Hey everyone! Don't forget about our team meeting tomorrow at 10 AM 📅" \
    -F "groupId=5" \
    -F "tags[0]=meeting" \
    -F "tags[1]=team" \
    -F "visibility=public"

# Test 5: Followers-Only Post
create_post "Followers-Only Post" \
    -F "content=This is a private update for my followers only. Thanks for following me! ❤️" \
    -F "tags[0]=update" \
    -F "visibility=followers"

# Test 6: Private Post
create_post "Private Post (Only Me)" \
    -F "content=Personal notes: Remember to finish the project documentation by Friday." \
    -F "visibility=me"

# Test 7: Long-Form Rich Content
create_post "Long-Form Rich Content Post" \
    -F "content=🚀 Excited to share my journey learning Spring Boot!

After 3 months of dedicated learning, I've built a full-featured social media application with:
✅ RESTful APIs
✅ Spring Security with JWT
✅ WebSocket for real-time features
✅ PostgreSQL database
✅ File upload handling
✅ Caching with Caffeine
✅ Async processing

Special thanks to @mentor and the amazing #java community for all the support! 💪

Check out my GitHub repo (link in bio) 🔗

#SpringBoot #Java #BackendDevelopment #WebDevelopment #Learning #TechJourney" \
    -F "tags[0]=SpringBoot" \
    -F "tags[1]=Java" \
    -F "tags[2]=BackendDevelopment" \
    -F "tags[3]=WebDevelopment" \
    -F "tags[4]=Learning" \
    -F "tags[5]=TechJourney" \
    -F "mentions[0]=mentor" \
    -F "visibility=public"

# Create multiple posts for feed testing
echo "================================================"
print_info "Creating multiple posts for feed testing..."
echo "================================================"

SAMPLE_POSTS=(
    "Just finished debugging the toughest bug of my career! 🐛💻"
    "Coffee + Code = Productivity ☕️👨‍💻"
    "Who else is excited about the new Java features? 🔥"
    "Reminder: Take breaks! Your mental health matters. 🧘‍♂️"
    "Shipping new features today! So proud of the team. 🚢"
)

for i in "${!SAMPLE_POSTS[@]}"; do
    NUM=$((i + 8))
    print_info "Creating post $NUM..."
    
    curl -s -X POST "$BASE_URL/api/posts" \
        -H "Authorization: Bearer $TOKEN" \
        -F "content=${SAMPLE_POSTS[$i]}" \
        -F "tags[0]=life" \
        -F "tags[1]=coding" \
        -F "visibility=public" > /dev/null
    
    print_success "Post $NUM created"
done

echo ""
echo "###############################################"
echo "#  TEST DATA GENERATION COMPLETE!            #"
echo "###############################################"
echo ""
print_success "All test posts have been created!"
echo ""
print_info "View posts at: $BASE_URL/api/posts/feed"
echo ""
